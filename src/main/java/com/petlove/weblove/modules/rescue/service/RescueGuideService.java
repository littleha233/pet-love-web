package com.petlove.weblove.modules.rescue.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.ops.enums.CityFeatureKey;
import com.petlove.weblove.modules.risk.RiskGuard;
import com.petlove.weblove.modules.rescue.dto.user.RescueGuideDetailDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueGuideListItemDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueGuideListQuery;
import com.petlove.weblove.modules.rescue.entity.RescueGuide;
import com.petlove.weblove.modules.rescue.enums.RescueGuideScenarioCode;
import com.petlove.weblove.modules.rescue.enums.RescueGuideStatus;
import com.petlove.weblove.modules.rescue.repository.RescueGuideRepository;
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
public class RescueGuideService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final RescueGuideRepository rescueGuideRepository;
    private final RiskGuard riskGuard;
    private final ObjectMapper objectMapper;

    public RescueGuideService(RescueGuideRepository rescueGuideRepository,
                              RiskGuard riskGuard,
                              ObjectMapper objectMapper) {
        this.rescueGuideRepository = rescueGuideRepository;
        this.riskGuard = riskGuard;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<RescueGuideListItemDTO> list(RescueGuideListQuery query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);

        RescueGuideScenarioCode scenarioCode = parseScenarioCode(query.getScenarioCode(), false);
        String cityCode = normalizeText(query.getCityCode());
        String keyword = normalizeText(query.getKeyword());
        Set<String> readBlockedCityCodes = cityCode == null
            ? riskGuard.resolveReadBlockedCityCodes(CityFeatureKey.RESCUE_GUIDE)
            : Collections.emptySet();
        if (cityCode != null) {
            riskGuard.ensureCityFeatureReadable(cityCode, CityFeatureKey.RESCUE_GUIDE);
        }

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.ASC, "sortOrder")
                .and(Sort.by(Sort.Direction.DESC, "publishedAt"))
                .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Specification<RescueGuide> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), RescueGuideStatus.PUBLISHED));

            if (scenarioCode != null) {
                predicates.add(cb.equal(root.get("scenarioCode"), scenarioCode));
            }

            if (cityCode != null) {
                predicates.add(cb.or(
                    cb.equal(root.get("cityCode"), cityCode),
                    cb.isNull(root.get("cityCode"))
                ));
            } else if (!readBlockedCityCodes.isEmpty()) {
                predicates.add(cb.or(
                    cb.isNull(root.get("cityCode")),
                    cb.not(root.get("cityCode").in(readBlockedCityCodes))
                ));
            }

            if (keyword != null) {
                String like = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("summary")), like),
                    cb.like(cb.lower(root.get("contentMd")), like)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<RescueGuide> guidePage = rescueGuideRepository.findAll(spec, pageable);
        if (guidePage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<RescueGuideListItemDTO> items = guidePage.getContent().stream()
            .map(this::toListItemDTO)
            .toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, guidePage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public RescueGuideDetailDTO detail(Long guideId) {
        RescueGuide guide = rescueGuideRepository.findByIdAndStatus(guideId, RescueGuideStatus.PUBLISHED)
            .orElseThrow(() -> new BizException(
                ErrorCode.RESCUE_GUIDE_NOT_FOUND,
                "Rescue guide not found"
            ));
        if (guide.getCityCode() != null && !guide.getCityCode().isBlank()) {
            riskGuard.ensureCityFeatureReadable(guide.getCityCode(), CityFeatureKey.RESCUE_GUIDE);
        }
        return toDetailDTO(guide);
    }

    private RescueGuideListItemDTO toListItemDTO(RescueGuide guide) {
        return new RescueGuideListItemDTO(
            guide.getId(),
            guide.getScenarioCode() == null ? null : guide.getScenarioCode().name(),
            guide.getTitle(),
            guide.getSummary(),
            guide.getCityCode(),
            fromJsonList(guide.getTags()),
            guide.getPublishedAt(),
            guide.getSortOrder()
        );
    }

    private RescueGuideDetailDTO toDetailDTO(RescueGuide guide) {
        return new RescueGuideDetailDTO(
            guide.getId(),
            guide.getScenarioCode() == null ? null : guide.getScenarioCode().name(),
            guide.getTitle(),
            guide.getSummary(),
            guide.getContentMd(),
            guide.getCityCode(),
            fromJsonList(guide.getTags()),
            guide.getPublishedAt(),
            guide.getUpdatedAt()
        );
    }

    private RescueGuideScenarioCode parseScenarioCode(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "scenarioCode is required");
            }
            return null;
        }
        try {
            return RescueGuideScenarioCode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "scenarioCode is invalid");
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
