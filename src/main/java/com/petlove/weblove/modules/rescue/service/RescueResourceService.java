package com.petlove.weblove.modules.rescue.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.MaskUtil;
import com.petlove.weblove.modules.ops.enums.CityFeatureKey;
import com.petlove.weblove.modules.risk.RiskGuard;
import com.petlove.weblove.modules.rescue.dto.user.RescueResourceDetailDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueResourceListItemDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueResourceListQuery;
import com.petlove.weblove.modules.rescue.entity.RescueResource;
import com.petlove.weblove.modules.rescue.enums.RescuePetType;
import com.petlove.weblove.modules.rescue.enums.RescueResourceStatus;
import com.petlove.weblove.modules.rescue.enums.RescueResourceType;
import com.petlove.weblove.modules.rescue.repository.RescueResourceRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RescueResourceService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final RescueResourceRepository rescueResourceRepository;
    private final RiskGuard riskGuard;
    private final ObjectMapper objectMapper;

    public RescueResourceService(RescueResourceRepository rescueResourceRepository,
                                 RiskGuard riskGuard,
                                 ObjectMapper objectMapper) {
        this.rescueResourceRepository = rescueResourceRepository;
        this.riskGuard = riskGuard;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<RescueResourceListItemDTO> list(RescueResourceListQuery query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);

        String cityCode = normalizeText(query.getCityCode());
        String keyword = normalizeText(query.getKeyword());
        RescueResourceType resourceType = parseResourceType(query.getResourceType(), false);
        RescuePetType petType = parsePetType(query.getPetType(), false);
        Set<String> readBlockedCityCodes = cityCode == null
            ? riskGuard.resolveReadBlockedCityCodes(CityFeatureKey.RESCUE_RESOURCE)
            : Collections.emptySet();
        if (cityCode != null) {
            riskGuard.ensureCityFeatureReadable(cityCode, CityFeatureKey.RESCUE_RESOURCE);
        }

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.ASC, "sortOrder")
                .and(Sort.by(Sort.Direction.DESC, "verifiedAt"))
                .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Specification<RescueResource> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), RescueResourceStatus.ACTIVE));

            if (cityCode != null) {
                predicates.add(cb.equal(root.get("cityCode"), cityCode));
            } else if (!readBlockedCityCodes.isEmpty()) {
                predicates.add(cb.not(root.get("cityCode").in(readBlockedCityCodes)));
            }
            if (resourceType != null) {
                predicates.add(cb.equal(root.get("resourceType"), resourceType));
            }
            if (petType != null) {
                String jsonValue = "\"" + petType.name() + "\"";
                predicates.add(cb.equal(
                    cb.function("JSON_CONTAINS", Integer.class, root.get("acceptPetTypes"), cb.literal(jsonValue)),
                    1
                ));
            }
            if (keyword != null) {
                String like = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("districtName")), like),
                    cb.like(cb.lower(root.get("address")), like),
                    cb.like(cb.lower(root.get("serviceScope")), like),
                    cb.like(cb.lower(root.get("description")), like)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<RescueResource> resourcePage = rescueResourceRepository.findAll(spec, pageable);
        if (resourcePage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<RescueResourceListItemDTO> items = resourcePage.getContent().stream()
            .map(this::toListItemDTO)
            .toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, resourcePage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public RescueResourceDetailDTO detail(Long resourceId) {
        RescueResource resource = rescueResourceRepository.findByIdAndStatus(resourceId, RescueResourceStatus.ACTIVE)
            .orElseThrow(() -> new BizException(
                ErrorCode.RESCUE_RESOURCE_NOT_FOUND,
                "Rescue resource not found"
            ));
        riskGuard.ensureCityFeatureReadable(resource.getCityCode(), CityFeatureKey.RESCUE_RESOURCE);
        return toDetailDTO(resource);
    }

    private RescueResourceListItemDTO toListItemDTO(RescueResource resource) {
        return new RescueResourceListItemDTO(
            resource.getId(),
            resource.getResourceType() == null ? null : resource.getResourceType().name(),
            resource.getName(),
            resource.getCityCode(),
            resource.getCityName(),
            resource.getDistrictName(),
            resource.getServiceScope(),
            fromJsonList(resource.getAcceptPetTypes()),
            fromJsonList(resource.getCapabilityTags()),
            maskContactPhone(resource.getContactPhone()),
            resource.getVerifiedAt(),
            resource.getSortOrder()
        );
    }

    private RescueResourceDetailDTO toDetailDTO(RescueResource resource) {
        return new RescueResourceDetailDTO(
            resource.getId(),
            resource.getResourceType() == null ? null : resource.getResourceType().name(),
            resource.getName(),
            resource.getCityCode(),
            resource.getCityName(),
            resource.getDistrictName(),
            resource.getAddress(),
            resource.getContactPhone(),
            resource.getContactWechat(),
            resource.getContactOther(),
            resource.getServiceHours(),
            resource.getServiceScope(),
            fromJsonList(resource.getAcceptPetTypes()),
            fromJsonList(resource.getCapabilityTags()),
            resource.getDescription(),
            resource.getSourceUrl(),
            resource.getVerifiedAt()
        );
    }

    private String maskContactPhone(String contactPhone) {
        if (contactPhone == null || contactPhone.isBlank()) {
            return null;
        }
        return MaskUtil.maskMobile(contactPhone.trim());
    }

    private RescueResourceType parseResourceType(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "resourceType is required");
            }
            return null;
        }
        try {
            return RescueResourceType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "resourceType is invalid");
        }
    }

    private RescuePetType parsePetType(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "petType is required");
            }
            return null;
        }
        try {
            return RescuePetType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "petType is invalid");
        }
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST_TYPE);
            if (values == null) {
                return Collections.emptyList();
            }
            return values;
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
