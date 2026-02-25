package com.petlove.weblove.modules.rescue.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.MaskUtil;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.enums.FileBizType;
import com.petlove.weblove.modules.file.enums.FileStatus;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.ops.enums.CityFeatureKey;
import com.petlove.weblove.modules.rescue.dto.user.MyRescueClueQuery;
import com.petlove.weblove.modules.rescue.dto.user.RescueClueDetailDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueClueListItemDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueCluePhotoDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueResourceSuggestionDTO;
import com.petlove.weblove.modules.rescue.dto.user.SubmitRescueClueRequest;
import com.petlove.weblove.modules.rescue.entity.RescueClue;
import com.petlove.weblove.modules.rescue.entity.RescueClueMedia;
import com.petlove.weblove.modules.rescue.entity.RescueResource;
import com.petlove.weblove.modules.rescue.enums.RescueClueStatus;
import com.petlove.weblove.modules.rescue.enums.RescuePetConditionTag;
import com.petlove.weblove.modules.rescue.enums.RescuePetType;
import com.petlove.weblove.modules.rescue.enums.RescueUrgencyLevel;
import com.petlove.weblove.modules.rescue.repository.RescueClueMediaRepository;
import com.petlove.weblove.modules.rescue.repository.RescueClueRepository;
import com.petlove.weblove.modules.rescue.repository.RescueResourceRepository;
import com.petlove.weblove.modules.risk.RiskActionKeys;
import com.petlove.weblove.modules.risk.RiskGuard;
import com.petlove.weblove.modules.system.entity.City;
import com.petlove.weblove.modules.system.repository.CityRepository;
import com.petlove.weblove.security.SecurityUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RescueClueService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private static final TypeReference<List<Long>> LONG_LIST_TYPE = new TypeReference<>() {
    };

    private final RescueClueRepository rescueClueRepository;
    private final RescueClueMediaRepository rescueClueMediaRepository;
    private final RescueResourceRepository rescueResourceRepository;
    private final FileObjectRepository fileObjectRepository;
    private final CityRepository cityRepository;
    private final RiskGuard riskGuard;
    private final ObjectMapper objectMapper;

    public RescueClueService(RescueClueRepository rescueClueRepository,
                             RescueClueMediaRepository rescueClueMediaRepository,
                             RescueResourceRepository rescueResourceRepository,
                             FileObjectRepository fileObjectRepository,
                             CityRepository cityRepository,
                             RiskGuard riskGuard,
                             ObjectMapper objectMapper) {
        this.rescueClueRepository = rescueClueRepository;
        this.rescueClueMediaRepository = rescueClueMediaRepository;
        this.rescueResourceRepository = rescueResourceRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.cityRepository = cityRepository;
        this.riskGuard = riskGuard;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RescueClueDetailDTO submit(SubmitRescueClueRequest request) {
        long userId = SecurityUtils.currentUserId();
        ensureCityEnabled(request.getCityCode());
        riskGuard.ensureUserActionAllowed(
            userId,
            request.getCityCode(),
            RiskActionKeys.RESCUE_CLUE_SUBMIT,
            CityFeatureKey.RESCUE_CLUE_SUBMIT
        );

        List<Long> photoFileIds = normalizeFileIds(request.getPhotoFileIds(), 9);
        validateCluePhotoFiles(photoFileIds, userId);

        RescueClue clue = new RescueClue();
        clue.setClueNo(generateClueNo());
        clue.setReporterUserId(userId);
        clue.setCityCode(normalizeRequiredText(request.getCityCode(), "cityCode"));
        clue.setCityName(normalizeRequiredText(request.getCityName(), "cityName"));
        clue.setDistrictName(normalizeText(request.getDistrictName()));
        clue.setLocationText(normalizeRequiredText(request.getLocationText(), "locationText"));
        clue.setGeoLat(request.getGeoLat());
        clue.setGeoLng(request.getGeoLng());
        clue.setPetType(parsePetType(request.getPetType(), false));
        clue.setEstimatedCount(request.getEstimatedCount());
        clue.setUrgencyLevel(parseUrgencyLevel(request.getUrgencyLevel(), true));
        clue.setConditionTags(toJson(normalizeConditionTags(request.getConditionTags())));
        clue.setDescription(normalizeRequiredText(request.getDescription(), "description"));
        clue.setContactName(normalizeRequiredText(request.getContactName(), "contactName"));
        clue.setContactMobile(normalizeRequiredText(request.getContactMobile(), "contactMobile"));
        clue.setContactMobileMasked(MaskUtil.maskMobile(clue.getContactMobile()));
        clue.setStatus(RescueClueStatus.SUBMITTED);

        RescueClue saved = rescueClueRepository.save(clue);

        if (!photoFileIds.isEmpty()) {
            List<RescueClueMedia> medias = new ArrayList<>();
            for (int i = 0; i < photoFileIds.size(); i++) {
                RescueClueMedia media = new RescueClueMedia();
                media.setClueId(saved.getId());
                media.setFileObjectId(photoFileIds.get(i));
                media.setSortOrder(i);
                medias.add(media);
            }
            rescueClueMediaRepository.saveAll(medias);
        }

        return toDetailDTO(saved, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<RescueClueListItemDTO> myClues(MyRescueClueQuery query) {
        long userId = SecurityUtils.currentUserId();

        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);
        RescueClueStatus status = parseClueStatus(query.getStatus(), false);

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "updatedAt")
                .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<RescueClue> cluePage = status == null
            ? rescueClueRepository.findByReporterUserId(userId, pageable)
            : rescueClueRepository.findByReporterUserIdAndStatus(userId, status, pageable);

        if (cluePage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<RescueClueListItemDTO> items = cluePage.getContent().stream()
            .map(clue -> new RescueClueListItemDTO(
                clue.getId(),
                clue.getClueNo(),
                clue.getCityName(),
                clue.getDistrictName(),
                clue.getPetType() == null ? null : clue.getPetType().name(),
                clue.getUrgencyLevel() == null ? null : clue.getUrgencyLevel().name(),
                clue.getStatus() == null ? null : clue.getStatus().name(),
                clue.getCreatedAt(),
                clue.getUpdatedAt()
            ))
            .toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, cluePage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public RescueClueDetailDTO myDetail(Long clueId) {
        long userId = SecurityUtils.currentUserId();

        RescueClue clue = rescueClueRepository.findById(clueId)
            .orElseThrow(() -> new BizException(
                ErrorCode.RESCUE_CLUE_NOT_FOUND,
                "Rescue clue not found"
            ));

        if (!Objects.equals(clue.getReporterUserId(), userId)) {
            throw new BizException(
                ErrorCode.RESCUE_CLUE_NOT_OWNER,
                "Rescue clue is not owned by current user"
            );
        }

        return toDetailDTO(clue, true);
    }

    private RescueClueDetailDTO toDetailDTO(RescueClue clue, boolean maskContactMobile) {
        List<RescueClueMedia> mediaList = rescueClueMediaRepository.findByClueIdOrderBySortOrderAsc(clue.getId());
        Map<Long, FileObject> fileMap = toFileMap(mediaList.stream().map(RescueClueMedia::getFileObjectId).toList());

        List<RescueCluePhotoDTO> photos = mediaList.stream().map(media -> {
            FileObject fileObject = fileMap.get(media.getFileObjectId());
            return new RescueCluePhotoDTO(
                media.getFileObjectId(),
                fileObject != null ? fileObject.getPublicUrl() : null,
                media.getSortOrder()
            );
        }).toList();

        List<Long> suggestedResourceIds = fromJsonLongList(clue.getSuggestedResourceIds());
        Map<Long, RescueResource> resourceMap = suggestedResourceIds.isEmpty()
            ? Collections.emptyMap()
            : rescueResourceRepository.findByIdIn(suggestedResourceIds).stream()
                .collect(Collectors.toMap(RescueResource::getId, Function.identity()));

        List<RescueResourceSuggestionDTO> suggestedResources = suggestedResourceIds.stream()
            .map(resourceMap::get)
            .filter(Objects::nonNull)
            .map(resource -> new RescueResourceSuggestionDTO(
                resource.getId(),
                resource.getName(),
                resource.getResourceType() == null ? null : resource.getResourceType().name(),
                resource.getContactPhone()
            ))
            .toList();

        return new RescueClueDetailDTO(
            clue.getId(),
            clue.getClueNo(),
            clue.getCityCode(),
            clue.getCityName(),
            clue.getDistrictName(),
            clue.getLocationText(),
            clue.getPetType() == null ? null : clue.getPetType().name(),
            clue.getEstimatedCount(),
            clue.getUrgencyLevel() == null ? null : clue.getUrgencyLevel().name(),
            fromJsonStringList(clue.getConditionTags()),
            clue.getDescription(),
            clue.getContactName(),
            maskContactMobile ? clue.getContactMobileMasked() : clue.getContactMobile(),
            clue.getStatus() == null ? null : clue.getStatus().name(),
            clue.getTriageNote(),
            clue.getResolutionNote(),
            suggestedResources,
            photos,
            clue.getCreatedAt(),
            clue.getUpdatedAt()
        );
    }

    private void ensureCityEnabled(String cityCode) {
        String normalized = normalizeRequiredText(cityCode, "cityCode");
        City city = cityRepository.findByCityCode(normalized)
            .orElseThrow(() -> new BizException(ErrorCode.INVALID_PARAM, "cityCode is invalid"));
        if (!city.isEnabled()) {
            throw new BizException(ErrorCode.INVALID_PARAM, "city is disabled");
        }
    }

    private String generateClueNo() {
        String prefix = "RC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String clueNo;
        int retry = 0;
        do {
            int seq = ThreadLocalRandom.current().nextInt(100000, 999999);
            clueNo = prefix + seq;
            retry++;
        } while (rescueClueRepository.existsByClueNo(clueNo) && retry < 20);

        if (rescueClueRepository.existsByClueNo(clueNo)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to generate clue number");
        }
        return clueNo;
    }

    private List<Long> normalizeFileIds(List<Long> ids, int maxCount) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        if (ids.size() > maxCount) {
            throw new BizException(ErrorCode.INVALID_PARAM, "photoFileIds size exceeds limit");
        }

        List<Long> normalized = ids.stream()
            .filter(id -> id != null && id > 0)
            .distinct()
            .toList();

        if (normalized.size() != ids.size()) {
            throw new BizException(ErrorCode.RESCUE_CLUE_FILE_INVALID, "photoFileIds contains invalid value");
        }
        return normalized;
    }

    private void validateCluePhotoFiles(List<Long> photoFileIds, long userId) {
        if (photoFileIds.isEmpty()) {
            return;
        }

        Map<Long, FileObject> fileMap = toFileMap(photoFileIds);
        if (fileMap.size() != photoFileIds.size()) {
            throw new BizException(ErrorCode.RESCUE_CLUE_FILE_INVALID, "Rescue clue photo file not found");
        }

        for (Long fileId : photoFileIds) {
            FileObject fileObject = fileMap.get(fileId);
            if (fileObject.getStatus() != FileStatus.READY
                || (fileObject.getBizType() != FileBizType.RESCUE_CLUE && fileObject.getBizType() != FileBizType.OTHER)) {
                throw new BizException(ErrorCode.RESCUE_CLUE_FILE_INVALID, "Rescue clue photo file is invalid");
            }
            if (!Objects.equals(fileObject.getOwnerUserId(), userId)) {
                throw new BizException(ErrorCode.RESCUE_CLUE_FILE_NOT_OWNED, "Rescue clue photo file is not owned by user");
            }
        }
    }

    private Map<Long, FileObject> toFileMap(Collection<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return fileObjectRepository.findAllById(fileIds).stream()
            .collect(Collectors.toMap(FileObject::getId, Function.identity()));
    }

    private List<String> normalizeConditionTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String tag : tags) {
            String normalizedTag = normalizeText(tag);
            if (normalizedTag == null) {
                throw new BizException(ErrorCode.INVALID_PARAM, "conditionTags contains blank value");
            }
            String upper = normalizedTag.toUpperCase(Locale.ROOT);
            try {
                RescuePetConditionTag.valueOf(upper);
            } catch (IllegalArgumentException ex) {
                throw new BizException(ErrorCode.INVALID_PARAM, "conditionTags contains invalid value");
            }
            normalized.add(upper);
        }

        if (normalized.size() > 10) {
            throw new BizException(ErrorCode.INVALID_PARAM, "conditionTags size exceeds limit");
        }
        return new ArrayList<>(normalized);
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

    private RescueUrgencyLevel parseUrgencyLevel(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "urgencyLevel is required");
            }
            return null;
        }
        try {
            return RescueUrgencyLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "urgencyLevel is invalid");
        }
    }

    private RescueClueStatus parseClueStatus(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "status is required");
            }
            return null;
        }
        try {
            return RescueClueStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.RESCUE_CLUE_STATUS_INVALID, "Rescue clue status is invalid");
        }
    }

    private List<String> fromJsonStringList(String json) {
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

    private List<Long> fromJsonLongList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<Long> values = objectMapper.readValue(json, LONG_LIST_TYPE);
            if (values == null) {
                return Collections.emptyList();
            }
            return values;
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "JSON serialization failed");
        }
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw new BizException(ErrorCode.INVALID_PARAM, fieldName + " is required");
        }
        return normalized;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
