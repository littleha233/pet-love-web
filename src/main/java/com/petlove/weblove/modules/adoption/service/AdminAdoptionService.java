package com.petlove.weblove.modules.adoption.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.MaskUtil;
import com.petlove.weblove.modules.adoption.dto.admin.AdminAdoptionPostDetailDTO;
import com.petlove.weblove.modules.adoption.dto.admin.AdminAdoptionPostListItemDTO;
import com.petlove.weblove.modules.adoption.dto.admin.AdminAdoptionPostQuery;
import com.petlove.weblove.modules.adoption.dto.admin.ApproveAdoptionPostRequest;
import com.petlove.weblove.modules.adoption.dto.admin.OfflineAdoptionPostRequest;
import com.petlove.weblove.modules.adoption.dto.admin.RejectAdoptionPostRequest;
import com.petlove.weblove.modules.adoption.entity.AdoptionPost;
import com.petlove.weblove.modules.adoption.entity.Pet;
import com.petlove.weblove.modules.adoption.entity.PetMedia;
import com.petlove.weblove.modules.adoption.enums.AdoptionPostStatus;
import com.petlove.weblove.modules.adoption.enums.PetType;
import com.petlove.weblove.modules.adoption.repository.AdoptionApplicationRepository;
import com.petlove.weblove.modules.adoption.repository.AdoptionPostRepository;
import com.petlove.weblove.modules.adoption.repository.PetMediaRepository;
import com.petlove.weblove.modules.adoption.repository.PetRepository;
import com.petlove.weblove.modules.admin.entity.AdminUser;
import com.petlove.weblove.modules.admin.service.AdminAuditService;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.user.entity.User;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.modules.user.repository.UserRepository;
import com.petlove.weblove.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAdoptionService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final AdoptionPostRepository adoptionPostRepository;
    private final PetRepository petRepository;
    private final PetMediaRepository petMediaRepository;
    private final AdoptionApplicationRepository adoptionApplicationRepository;
    private final FileObjectRepository fileObjectRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final com.petlove.weblove.modules.admin.repository.AdminUserRepository adminUserRepository;
    private final AdminAuditService adminAuditService;
    private final ObjectMapper objectMapper;

    public AdminAdoptionService(AdoptionPostRepository adoptionPostRepository,
                                PetRepository petRepository,
                                PetMediaRepository petMediaRepository,
                                AdoptionApplicationRepository adoptionApplicationRepository,
                                FileObjectRepository fileObjectRepository,
                                UserRepository userRepository,
                                UserProfileRepository userProfileRepository,
                                com.petlove.weblove.modules.admin.repository.AdminUserRepository adminUserRepository,
                                AdminAuditService adminAuditService,
                                ObjectMapper objectMapper) {
        this.adoptionPostRepository = adoptionPostRepository;
        this.petRepository = petRepository;
        this.petMediaRepository = petMediaRepository;
        this.adoptionApplicationRepository = adoptionApplicationRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.adminUserRepository = adminUserRepository;
        this.adminAuditService = adminAuditService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR')")
    public PageResponse<AdminAdoptionPostListItemDTO> list(AdminAdoptionPostQuery query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);

        AdoptionPostStatus status = parsePostStatus(query.getStatus(), false);
        PetType petType = parsePetType(query.getPetType(), false);
        String cityCode = normalizeText(query.getCityCode());
        String keyword = normalizeText(query.getKeyword());
        Set<Long> keywordUserIds = resolveKeywordUserIds(keyword);

        LocalDate dateFrom = query.getDateFrom();
        LocalDate dateTo = query.getDateTo();
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new BizException(ErrorCode.INVALID_PARAM, "dateFrom cannot be greater than dateTo");
        }

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Specification<AdoptionPost> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (cityCode != null) {
                predicates.add(cb.equal(root.get("cityCode"), cityCode));
            }
            if (petType != null && criteriaQuery != null) {
                predicates.add(root.get("petId").in(buildPetTypeSubquery(criteriaQuery, root, cb, petType)));
            }
            if (keyword != null) {
                String like = "%" + keyword.toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), like);
                if (keywordUserIds != null && !keywordUserIds.isEmpty()) {
                    predicates.add(cb.or(titleLike, root.get("publisherUserId").in(keywordUserIds)));
                } else {
                    predicates.add(titleLike);
                }
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThan(root.get("updatedAt"), dateTo.plusDays(1).atStartOfDay()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<AdoptionPost> postPage = adoptionPostRepository.findAll(spec, pageable);
        if (postPage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<AdoptionPost> posts = postPage.getContent();
        Set<Long> publisherIds = posts.stream().map(AdoptionPost::getPublisherUserId).collect(Collectors.toSet());
        Set<Long> petIds = posts.stream().map(AdoptionPost::getPetId).collect(Collectors.toSet());
        Set<Long> reviewedAdminIds = posts.stream()
            .map(AdoptionPost::getReviewedByAdminId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<Long, UserProfile> profileMap = userProfileRepository.findByUserIdIn(publisherIds)
            .stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
        Map<Long, Pet> petMap = petRepository.findAllById(petIds)
            .stream()
            .collect(Collectors.toMap(Pet::getId, Function.identity()));
        Map<Long, String> adminNameMap = adminUserRepository.findAllById(reviewedAdminIds)
            .stream()
            .collect(Collectors.toMap(AdminUser::getId, AdminUser::getDisplayName));

        List<AdminAdoptionPostListItemDTO> items = posts.stream().map(post -> {
            UserProfile profile = profileMap.get(post.getPublisherUserId());
            Pet pet = petMap.get(post.getPetId());
            return new AdminAdoptionPostListItemDTO(
                post.getId(),
                post.getTitle(),
                post.getStatus().name(),
                post.getPublisherUserId(),
                profile != null && profile.getNickname() != null
                    ? profile.getNickname()
                    : "用户" + post.getPublisherUserId(),
                post.getCityName(),
                pet != null && pet.getPetType() != null ? pet.getPetType().name() : null,
                post.getSubmitVersion() == null ? 1 : post.getSubmitVersion(),
                post.getReviewedByAdminId() != null ? adminNameMap.get(post.getReviewedByAdminId()) : null,
                post.getReviewedAt(),
                post.getUpdatedAt()
            );
        }).toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, postPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR')")
    public AdminAdoptionPostDetailDTO detail(Long postId) {
        AdoptionPost post = adoptionPostRepository.findById(postId)
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Adoption post not found"));

        Pet pet = petRepository.findById(post.getPetId())
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Pet not found"));

        List<PetMedia> medias = petMediaRepository.findByPetIdOrderBySortOrderAsc(pet.getId());
        Map<Long, FileObject> fileMap = fileObjectRepository.findAllById(
                medias.stream().map(PetMedia::getFileObjectId).toList())
            .stream()
            .collect(Collectors.toMap(FileObject::getId, Function.identity()));
        List<String> petImages = medias.stream()
            .map(media -> {
                FileObject fileObject = fileMap.get(media.getFileObjectId());
                return fileObject != null ? fileObject.getPublicUrl() : null;
            })
            .filter(Objects::nonNull)
            .toList();

        User user = userRepository.findById(post.getPublisherUserId()).orElse(null);
        UserProfile profile = userProfileRepository.findByUserId(post.getPublisherUserId()).orElse(null);

        String reviewedByAdminName = null;
        if (post.getReviewedByAdminId() != null) {
            reviewedByAdminName = adminUserRepository.findById(post.getReviewedByAdminId())
                .map(AdminUser::getDisplayName)
                .orElse(null);
        }

        return new AdminAdoptionPostDetailDTO(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getStatus().name(),
            post.getSubmitVersion() == null ? 1 : post.getSubmitVersion(),
            post.getPublisherUserId(),
            profile != null && profile.getNickname() != null ? profile.getNickname() : "用户" + post.getPublisherUserId(),
            user != null ? MaskUtil.maskMobile(user.getMobile()) : null,
            post.getCityCode(),
            post.getCityName(),
            post.getDistrictName(),
            pet.getPetType() != null ? pet.getPetType().name() : null,
            pet.getName(),
            pet.getGender() != null ? pet.getGender().name() : null,
            pet.getAgeMonths(),
            pet.getBreed(),
            pet.getWeightKg(),
            pet.getNeuteredStatus() != null ? pet.getNeuteredStatus().name() : null,
            pet.getVaccinatedStatus() != null ? pet.getVaccinatedStatus().name() : null,
            pet.getHealthNote(),
            fromJsonList(pet.getTemperamentTags()),
            pet.getSpecialCareNote(),
            petImages,
            Math.toIntExact(adoptionApplicationRepository.countByPostId(post.getId())),
            post.getRejectReasonCode(),
            post.getRejectReasonText(),
            reviewedByAdminName,
            post.getReviewedAt(),
            post.getPublishedAt(),
            post.getClosedAt(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR')")
    @Transactional
    public void approve(Long postId, ApproveAdoptionPostRequest request) {
        long adminId = SecurityUtils.currentAdminId();
        AdoptionPost post = adoptionPostRepository.findById(postId)
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Adoption post not found"));

        if (post.getStatus() != AdoptionPostStatus.PENDING_REVIEW) {
            throw new BizException(ErrorCode.ADOPTION_POST_REVIEW_NOT_ALLOWED, "Only pending post can be approved");
        }

        Map<String, Object> before = auditSnapshot(post);
        LocalDateTime now = LocalDateTime.now();

        post.setStatus(AdoptionPostStatus.PUBLISHED);
        post.setReviewedByAdminId(adminId);
        post.setReviewedAt(now);
        post.setPublishedAt(now);
        post.setRejectReasonCode(null);
        post.setRejectReasonText(null);
        adoptionPostRepository.save(post);

        adminAuditService.writeAuditLog(
            adminId,
            "APPROVE_ADOPTION_POST",
            "ADOPTION_POST",
            String.valueOf(postId),
            before,
            auditSnapshot(post),
            request != null ? normalizeText(request.getRemark()) : null
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR')")
    @Transactional
    public void reject(Long postId, RejectAdoptionPostRequest request) {
        long adminId = SecurityUtils.currentAdminId();
        AdoptionPost post = adoptionPostRepository.findById(postId)
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Adoption post not found"));

        if (post.getStatus() != AdoptionPostStatus.PENDING_REVIEW) {
            throw new BizException(ErrorCode.ADOPTION_POST_REVIEW_NOT_ALLOWED, "Only pending post can be rejected");
        }

        Map<String, Object> before = auditSnapshot(post);

        post.setStatus(AdoptionPostStatus.REJECTED);
        post.setRejectReasonCode(normalizeRequiredText(request.getRejectReasonCode()));
        post.setRejectReasonText(normalizeRequiredText(request.getRejectReasonText()));
        post.setReviewedByAdminId(adminId);
        post.setReviewedAt(LocalDateTime.now());
        adoptionPostRepository.save(post);

        adminAuditService.writeAuditLog(
            adminId,
            "REJECT_ADOPTION_POST",
            "ADOPTION_POST",
            String.valueOf(postId),
            before,
            auditSnapshot(post),
            normalizeText(request.getRemark())
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR')")
    @Transactional
    public void offline(Long postId, OfflineAdoptionPostRequest request) {
        long adminId = SecurityUtils.currentAdminId();
        AdoptionPost post = adoptionPostRepository.findById(postId)
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Adoption post not found"));

        if (post.getStatus() != AdoptionPostStatus.PUBLISHED
            && post.getStatus() != AdoptionPostStatus.CLOSED
            && post.getStatus() != AdoptionPostStatus.REJECTED) {
            throw new BizException(ErrorCode.ADOPTION_POST_REVIEW_NOT_ALLOWED, "Current status does not allow offline");
        }

        Map<String, Object> before = auditSnapshot(post);

        post.setStatus(AdoptionPostStatus.OFFLINE);
        post.setReviewedByAdminId(adminId);
        post.setReviewedAt(LocalDateTime.now());
        adoptionPostRepository.save(post);

        adminAuditService.writeAuditLog(
            adminId,
            "OFFLINE_ADOPTION_POST",
            "ADOPTION_POST",
            String.valueOf(postId),
            before,
            auditSnapshot(post),
            normalizeRequiredText(request.getReason())
        );
    }

    private Set<Long> resolveKeywordUserIds(String keyword) {
        if (keyword == null) {
            return null;
        }
        Set<Long> userIds = new HashSet<>();
        if (keyword.matches("\\d+")) {
            try {
                userIds.add(Long.parseLong(keyword));
            } catch (NumberFormatException ignored) {
                // ignore parse failure
            }
        }
        userIds.addAll(userRepository.searchIdsByMobileOrEmail(keyword));
        userIds.addAll(userProfileRepository.searchUserIdsByNickname(keyword));
        return userIds;
    }

    private Subquery<Long> buildPetTypeSubquery(jakarta.persistence.criteria.CriteriaQuery<?> criteriaQuery,
                                                 Root<AdoptionPost> postRoot,
                                                 jakarta.persistence.criteria.CriteriaBuilder cb,
                                                 PetType petType) {
        Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
        Root<Pet> petRoot = subquery.from(Pet.class);
        subquery.select(petRoot.get("id"));
        subquery.where(petRoot.get("id").in(postRoot.get("petId")), cb.equal(petRoot.get("petType"), petType));
        return subquery;
    }

    private AdoptionPostStatus parsePostStatus(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "status is required");
            }
            return null;
        }
        try {
            return AdoptionPostStatus.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "status is invalid");
        }
    }

    private PetType parsePetType(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "petType is required");
            }
            return null;
        }
        try {
            return PetType.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "petType is invalid");
        }
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to parse JSON field");
        }
    }

    private Map<String, Object> auditSnapshot(AdoptionPost post) {
        Map<String, Object> data = new HashMap<>();
        data.put("postId", post.getId());
        data.put("status", post.getStatus().name());
        data.put("submitVersion", post.getSubmitVersion());
        data.put("rejectReasonCode", post.getRejectReasonCode());
        data.put("rejectReasonText", post.getRejectReasonText());
        data.put("reviewedByAdminId", post.getReviewedByAdminId());
        data.put("reviewedAt", post.getReviewedAt());
        data.put("publishedAt", post.getPublishedAt());
        data.put("closedAt", post.getClosedAt());
        data.put("updatedAt", post.getUpdatedAt());
        return data;
    }

    private String normalizeRequiredText(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw new BizException(ErrorCode.INVALID_PARAM, "Required text is missing");
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
