package com.petlove.weblove.modules.feeding.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.adoption.enums.PetType;
import com.petlove.weblove.modules.feeding.dto.provider.UpsertFeedingProviderProfileRequest;
import com.petlove.weblove.modules.feeding.dto.user.FeedingProviderDetailDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingProviderListItemDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingProviderViewerContextDTO;
import com.petlove.weblove.modules.feeding.entity.FeedingProviderProfile;
import com.petlove.weblove.modules.feeding.enums.FeedingProviderProfileStatus;
import com.petlove.weblove.modules.feeding.enums.FeedingServiceItemTag;
import com.petlove.weblove.modules.feeding.repository.FeedingProviderProfileRepository;
import com.petlove.weblove.modules.system.entity.City;
import com.petlove.weblove.modules.system.repository.CityRepository;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.security.AuthPrincipal;
import com.petlove.weblove.security.AuthPrincipalType;
import com.petlove.weblove.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedingProviderService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final FeedingProviderProfileRepository feedingProviderProfileRepository;
    private final UserProfileRepository userProfileRepository;
    private final CityRepository cityRepository;
    private final ObjectMapper objectMapper;

    public FeedingProviderService(FeedingProviderProfileRepository feedingProviderProfileRepository,
                                  UserProfileRepository userProfileRepository,
                                  CityRepository cityRepository,
                                  ObjectMapper objectMapper) {
        this.feedingProviderProfileRepository = feedingProviderProfileRepository;
        this.userProfileRepository = userProfileRepository;
        this.cityRepository = cityRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<FeedingProviderListItemDTO> list(int page,
                                                         int pageSize,
                                                         String cityCode,
                                                         String petType,
                                                         String keyword,
                                                         String sortBy) {
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);
        String normalizedCityCode = normalizeText(cityCode);
        String normalizedKeyword = normalizeText(keyword);
        PetType parsedPetType = parsePetType(petType, false);

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            resolveSort(sortBy)
        );

        Set<Long> keywordUserIds = resolveKeywordUserIds(normalizedKeyword);

        Specification<FeedingProviderProfile> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), FeedingProviderProfileStatus.ACTIVE));

            if (normalizedCityCode != null) {
                predicates.add(cb.equal(root.get("serviceCityCode"), normalizedCityCode));
            }

            if (parsedPetType != null) {
                String jsonValue = "\"" + parsedPetType.name() + "\"";
                predicates.add(cb.equal(
                    cb.function("JSON_CONTAINS", Integer.class, root.get("servicePetTypes"), cb.literal(jsonValue)),
                    1
                ));
            }

            if (normalizedKeyword != null) {
                String like = "%" + normalizedKeyword.toLowerCase(Locale.ROOT) + "%";
                List<Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(cb.like(cb.lower(root.get("displayName")), like));
                keywordPredicates.add(cb.like(cb.lower(root.get("headline")), like));
                keywordPredicates.add(cb.like(cb.lower(root.get("intro")), like));
                if (!keywordUserIds.isEmpty()) {
                    keywordPredicates.add(root.get("providerUserId").in(keywordUserIds));
                }
                predicates.add(cb.or(keywordPredicates.toArray(Predicate[]::new)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<FeedingProviderProfile> profilePage = feedingProviderProfileRepository.findAll(spec, pageable);
        if (profilePage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<FeedingProviderProfile> profiles = profilePage.getContent();
        Map<Long, UserProfile> profileMap = userProfileRepository.findByUserIdIn(
                profiles.stream().map(FeedingProviderProfile::getProviderUserId).toList())
            .stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        List<FeedingProviderListItemDTO> items = profiles.stream().map(profile -> {
            UserProfile userProfile = profileMap.get(profile.getProviderUserId());
            return new FeedingProviderListItemDTO(
                profile.getProviderUserId(),
                profile.getId(),
                displayName(profile, userProfile),
                profile.getHeadline(),
                userProfile != null ? userProfile.getAvatarUrl() : null,
                profile.getServiceCityCode(),
                profile.getServiceCityName(),
                fromJsonList(profile.getServicePetTypes()),
                fromJsonList(profile.getServiceItemTags()),
                profile.getBasePricePerVisit(),
                profile.getRatingAvg(),
                profile.getRatingCount(),
                profile.getCompletedOrderCount()
            );
        }).toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, profilePage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public FeedingProviderDetailDTO detail(Long providerUserId) {
        FeedingProviderProfile profile = feedingProviderProfileRepository
            .findByProviderUserIdAndStatus(providerUserId, FeedingProviderProfileStatus.ACTIVE)
            .orElseThrow(() -> new BizException(
                ErrorCode.FEEDING_PROVIDER_PROFILE_NOT_FOUND,
                "Active feeding provider profile not found"
            ));

        UserProfile userProfile = userProfileRepository.findByUserId(providerUserId).orElse(null);
        return toDetail(profile, userProfile, buildViewerContext(profile.getProviderUserId()));
    }

    @Transactional(readOnly = true)
    public FeedingProviderDetailDTO myProfile() {
        long userId = SecurityUtils.currentUserId();
        ensureProviderVerified(userId);

        FeedingProviderProfile profile = feedingProviderProfileRepository.findByProviderUserId(userId).orElse(null);
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElse(null);

        if (profile == null) {
            return new FeedingProviderDetailDTO(
                userId,
                null,
                FeedingProviderProfileStatus.DRAFT.name(),
                userProfile != null ? userProfile.getNickname() : "",
                null,
                null,
                userProfile != null ? userProfile.getAvatarUrl() : null,
                userProfile != null ? userProfile.getCityCode() : null,
                userProfile != null ? userProfile.getCityName() : null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                null,
                java.math.BigDecimal.ZERO,
                0,
                0,
                null
            );
        }

        return toDetail(profile, userProfile, null);
    }

    @Transactional
    public FeedingProviderDetailDTO upsertMyProfile(UpsertFeedingProviderProfileRequest request) {
        long userId = SecurityUtils.currentUserId();
        UserProfile userProfile = ensureProviderVerified(userId);

        City city = cityRepository.findByCityCode(normalizeRequiredText(request.getServiceCityCode(), "serviceCityCode"))
            .orElseThrow(() -> new BizException(ErrorCode.INVALID_PARAM, "serviceCityCode is invalid"));
        if (!city.isEnabled()) {
            throw new BizException(ErrorCode.INVALID_PARAM, "service city is disabled");
        }

        List<String> servicePetTypes = normalizePetTypes(request.getServicePetTypes(), true);
        List<String> serviceItemTags = normalizeServiceItemTags(request.getServiceItemTags(), true);
        List<String> serviceDistricts = normalizeStringList(request.getServiceDistricts(), 64, 30);
        FeedingProviderProfileStatus targetStatus = parseProviderProfileStatus(request.getStatus(), false);

        FeedingProviderProfile profile = feedingProviderProfileRepository.findByProviderUserId(userId)
            .orElseGet(() -> {
                FeedingProviderProfile created = new FeedingProviderProfile();
                created.setProviderUserId(userId);
                created.setRatingAvg(java.math.BigDecimal.ZERO.setScale(2));
                created.setRatingCount(0);
                created.setCompletedOrderCount(0);
                created.setStatus(FeedingProviderProfileStatus.DRAFT);
                return created;
            });

        if (targetStatus == null) {
            targetStatus = profile.getStatus() == null ? FeedingProviderProfileStatus.DRAFT : profile.getStatus();
        }

        if (targetStatus == FeedingProviderProfileStatus.ACTIVE) {
            ensureProfileCompleteForActive(request, servicePetTypes, serviceItemTags);
        }

        profile.setStatus(targetStatus);
        profile.setDisplayName(normalizeText(request.getDisplayName()));
        profile.setHeadline(normalizeText(request.getHeadline()));
        profile.setIntro(normalizeText(request.getIntro()));
        profile.setServiceCityCode(city.getCityCode());
        profile.setServiceCityName(normalizeRequiredText(request.getServiceCityName(), "serviceCityName"));
        profile.setServiceDistricts(toJson(serviceDistricts));
        profile.setServicePetTypes(toJson(servicePetTypes));
        profile.setServiceItemTags(toJson(serviceItemTags));
        profile.setBasePricePerVisit(request.getBasePricePerVisit());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setMaxOrdersPerDay(request.getMaxOrdersPerDay());
        profile.setAcceptNotes(normalizeText(request.getAcceptNotes()));

        FeedingProviderProfile saved = feedingProviderProfileRepository.save(profile);
        return toDetail(saved, userProfile, null);
    }

    private Sort resolveSort(String sortBy) {
        String normalized = sortBy == null ? "DEFAULT" : sortBy.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "LATEST" -> Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"));
            case "RATING" -> Sort.by(Sort.Direction.DESC, "ratingAvg")
                .and(Sort.by(Sort.Direction.DESC, "ratingCount"))
                .and(Sort.by(Sort.Direction.DESC, "updatedAt"));
            case "DEFAULT" -> Sort.by(Sort.Direction.DESC, "ratingAvg")
                .and(Sort.by(Sort.Direction.DESC, "completedOrderCount"))
                .and(Sort.by(Sort.Direction.DESC, "updatedAt"));
            default -> throw new BizException(ErrorCode.INVALID_PARAM, "sortBy is invalid");
        };
    }

    private Set<Long> resolveKeywordUserIds(String keyword) {
        if (keyword == null) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(userProfileRepository.searchUserIdsByNickname(keyword));
    }

    private FeedingProviderDetailDTO toDetail(FeedingProviderProfile profile,
                                              UserProfile userProfile,
                                              FeedingProviderViewerContextDTO viewerContext) {
        return new FeedingProviderDetailDTO(
            profile.getProviderUserId(),
            profile.getId(),
            profile.getStatus() != null ? profile.getStatus().name() : null,
            displayName(profile, userProfile),
            profile.getHeadline(),
            profile.getIntro(),
            userProfile != null ? userProfile.getAvatarUrl() : null,
            profile.getServiceCityCode(),
            profile.getServiceCityName(),
            fromJsonList(profile.getServiceDistricts()),
            fromJsonList(profile.getServicePetTypes()),
            fromJsonList(profile.getServiceItemTags()),
            profile.getBasePricePerVisit(),
            profile.getExperienceYears(),
            profile.getAcceptNotes(),
            profile.getRatingAvg(),
            profile.getRatingCount(),
            profile.getCompletedOrderCount(),
            viewerContext
        );
    }

    private FeedingProviderViewerContextDTO buildViewerContext(Long providerUserId) {
        Long viewerUserId = currentUserIdOrNull();
        if (viewerUserId == null) {
            return new FeedingProviderViewerContextDTO(false, "请先登录");
        }
        if (Objects.equals(providerUserId, viewerUserId)) {
            return new FeedingProviderViewerContextDTO(false, "不能预约自己");
        }

        UserProfile viewerProfile = userProfileRepository.findByUserId(viewerUserId).orElse(null);
        if (viewerProfile == null || !viewerProfile.isRealNameVerified()) {
            return new FeedingProviderViewerContextDTO(false, "需先完成实名认证");
        }
        return new FeedingProviderViewerContextDTO(true, null);
    }

    private UserProfile ensureProviderVerified(long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new BizException(
                ErrorCode.FEEDING_PROVIDER_VERIFICATION_REQUIRED,
                "Provider verification is required"
            ));
        if (!userProfile.isProviderVerified()) {
            throw new BizException(
                ErrorCode.FEEDING_PROVIDER_VERIFICATION_REQUIRED,
                "Provider verification is required"
            );
        }
        return userProfile;
    }

    private void ensureProfileCompleteForActive(UpsertFeedingProviderProfileRequest request,
                                                List<String> servicePetTypes,
                                                List<String> serviceItemTags) {
        if (normalizeText(request.getIntro()) == null
            || normalizeText(request.getServiceCityCode()) == null
            || servicePetTypes.isEmpty()
            || serviceItemTags.isEmpty()) {
            throw new BizException(
                ErrorCode.FEEDING_PROVIDER_PROFILE_STATUS_INVALID,
                "Profile is incomplete for ACTIVE status"
            );
        }
    }

    private FeedingProviderProfileStatus parseProviderProfileStatus(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "provider profile status is required");
            }
            return null;
        }
        try {
            return FeedingProviderProfileStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(
                ErrorCode.FEEDING_PROVIDER_PROFILE_STATUS_INVALID,
                "provider profile status is invalid"
            );
        }
    }

    private PetType parsePetType(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "petType is required");
            }
            return null;
        }
        try {
            return PetType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "petType is invalid");
        }
    }

    private List<String> normalizePetTypes(List<String> values, boolean required) {
        if (values == null || values.isEmpty()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "servicePetTypes is required");
            }
            return Collections.emptyList();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String raw : values) {
            PetType petType = parsePetType(raw, true);
            normalized.add(petType.name());
        }
        return new ArrayList<>(normalized);
    }

    private List<String> normalizeServiceItemTags(List<String> values, boolean required) {
        if (values == null || values.isEmpty()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "serviceItemTags is required");
            }
            return Collections.emptyList();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String raw : values) {
            if (raw == null || raw.isBlank()) {
                throw new BizException(ErrorCode.INVALID_PARAM, "serviceItemTags contains blank value");
            }
            try {
                FeedingServiceItemTag tag = FeedingServiceItemTag.valueOf(raw.trim().toUpperCase(Locale.ROOT));
                normalized.add(tag.name());
            } catch (IllegalArgumentException ex) {
                throw new BizException(ErrorCode.INVALID_PARAM, "serviceItemTags contains invalid value");
            }
        }
        return new ArrayList<>(normalized);
    }

    private List<String> normalizeStringList(List<String> values, int maxLength, int maxItems) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        if (values.size() > maxItems) {
            throw new BizException(ErrorCode.INVALID_PARAM, "list size exceeds limit");
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String normalizedValue = normalizeText(value);
            if (normalizedValue == null) {
                continue;
            }
            if (normalizedValue.length() > maxLength) {
                throw new BizException(ErrorCode.INVALID_PARAM, "list item length exceeds limit");
            }
            normalized.add(normalizedValue);
        }
        return new ArrayList<>(normalized);
    }

    private String displayName(FeedingProviderProfile profile, UserProfile userProfile) {
        String displayName = normalizeText(profile.getDisplayName());
        if (displayName != null) {
            return displayName;
        }
        if (userProfile != null && normalizeText(userProfile.getNickname()) != null) {
            return userProfile.getNickname();
        }
        return "服务者" + profile.getProviderUserId();
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

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST_TYPE);
            return values == null ? Collections.emptyList() : values;
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to parse profile JSON field");
        }
    }

    private String toJson(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to serialize profile JSON field");
        }
    }

    private Long currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            return null;
        }
        return principal.type() == AuthPrincipalType.USER ? principal.id() : null;
    }
}
