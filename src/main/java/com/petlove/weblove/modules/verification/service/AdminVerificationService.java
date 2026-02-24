package com.petlove.weblove.modules.verification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.MaskUtil;
import com.petlove.weblove.modules.admin.entity.AdminUser;
import com.petlove.weblove.modules.admin.repository.AdminUserRepository;
import com.petlove.weblove.modules.admin.service.AdminAuditService;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.user.entity.User;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.modules.user.repository.UserRepository;
import com.petlove.weblove.modules.verification.dto.admin.AdminVerificationDetailDTO;
import com.petlove.weblove.modules.verification.dto.admin.AdminVerificationListItemDTO;
import com.petlove.weblove.modules.verification.dto.admin.AdminVerificationQuery;
import com.petlove.weblove.modules.verification.dto.admin.ApproveVerificationRequest;
import com.petlove.weblove.modules.verification.dto.admin.RejectVerificationRequest;
import com.petlove.weblove.modules.verification.entity.UserVerification;
import com.petlove.weblove.modules.verification.enums.VerificationStatus;
import com.petlove.weblove.modules.verification.enums.VerificationType;
import com.petlove.weblove.modules.verification.repository.UserVerificationRepository;
import com.petlove.weblove.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
public class AdminVerificationService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<Long>> LONG_LIST_TYPE = new TypeReference<>() {
    };

    private final UserVerificationRepository userVerificationRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AdminUserRepository adminUserRepository;
    private final FileObjectRepository fileObjectRepository;
    private final AdminAuditService adminAuditService;
    private final ObjectMapper objectMapper;

    public AdminVerificationService(UserVerificationRepository userVerificationRepository,
                                    UserRepository userRepository,
                                    UserProfileRepository userProfileRepository,
                                    AdminUserRepository adminUserRepository,
                                    FileObjectRepository fileObjectRepository,
                                    AdminAuditService adminAuditService,
                                    ObjectMapper objectMapper) {
        this.userVerificationRepository = userVerificationRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.adminUserRepository = adminUserRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.adminAuditService = adminAuditService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminVerificationListItemDTO> list(AdminVerificationQuery query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        Pageable pageable = PageRequest.of(
            Math.max(page - 1, 0),
            Math.min(Math.max(pageSize, 1), 100),
            Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        VerificationType verificationType = parseVerificationType(query.getVerificationType());
        VerificationStatus status = parseVerificationStatus(query.getStatus());

        Set<Long> keywordUserIds = resolveKeywordUserIds(query.getKeyword());
        if (query.getKeyword() != null && !query.getKeyword().isBlank() && keywordUserIds.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), page, pageSize, 0);
        }

        LocalDate dateFrom = query.getDateFrom();
        LocalDate dateTo = query.getDateTo();
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new BizException(ErrorCode.INVALID_PARAM, "dateFrom cannot be greater than dateTo");
        }

        Specification<UserVerification> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (verificationType != null) {
                predicates.add(cb.equal(root.get("verificationType"), verificationType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (query.getCityCode() != null && !query.getCityCode().isBlank()) {
                predicates.add(cb.equal(root.get("providerServiceCityCode"), query.getCityCode().trim()));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThan(root.get("updatedAt"), dateTo.plusDays(1).atStartOfDay()));
            }
            if (keywordUserIds != null) {
                predicates.add(root.get("userId").in(keywordUserIds));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<UserVerification> verificationPage = userVerificationRepository.findAll(spec, pageable);

        Set<Long> userIds = verificationPage.getContent().stream().map(UserVerification::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, UserProfile> profileMap = userProfileRepository.findByUserIdIn(userIds).stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        Set<Long> reviewedAdminIds = verificationPage.getContent().stream()
            .map(UserVerification::getReviewedByAdminId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, String> adminNameMap = adminUserRepository.findAllById(reviewedAdminIds).stream()
            .collect(Collectors.toMap(AdminUser::getId, AdminUser::getDisplayName));

        List<AdminVerificationListItemDTO> items = verificationPage.getContent().stream().map(verification -> {
            User user = userMap.get(verification.getUserId());
            UserProfile profile = profileMap.get(verification.getUserId());
            return new AdminVerificationListItemDTO(
                verification.getId(),
                verification.getUserId(),
                profile != null ? profile.getNickname() : null,
                user != null ? MaskUtil.maskMobile(user.getMobile()) : null,
                verification.getVerificationType().name(),
                verification.getStatus().name(),
                verification.getSubmitVersion(),
                verification.getProviderServiceCityCode(),
                verification.getReviewedByAdminId() != null ? adminNameMap.get(verification.getReviewedByAdminId()) : null,
                verification.getReviewedAt(),
                verification.getUpdatedAt()
            );
        }).toList();

        return new PageResponse<>(items, page, pageSize, verificationPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AdminVerificationDetailDTO detail(Long verificationId) {
        UserVerification verification = userVerificationRepository.findById(verificationId)
            .orElseThrow(() -> new BizException(ErrorCode.VERIFICATION_NOT_FOUND, "Verification not found"));

        User user = userRepository.findById(verification.getUserId())
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "User not found"));
        UserProfile profile = userProfileRepository.findByUserId(verification.getUserId()).orElse(null);

        String reviewedByAdminName = null;
        if (verification.getReviewedByAdminId() != null) {
            reviewedByAdminName = adminUserRepository.findById(verification.getReviewedByAdminId())
                .map(AdminUser::getDisplayName)
                .orElse(null);
        }

        return new AdminVerificationDetailDTO(
            verification.getId(),
            verification.getUserId(),
            profile != null ? profile.getNickname() : null,
            MaskUtil.maskMobile(user.getMobile()),
            MaskUtil.maskEmail(user.getEmail()),
            verification.getVerificationType().name(),
            verification.getStatus().name(),
            verification.getSubmitVersion(),
            verification.getRealName(),
            verification.getIdNoMasked(),
            resolveFileUrl(verification.getIdFrontFileId()),
            resolveFileUrl(verification.getIdBackFileId()),
            resolveFileUrl(verification.getHoldingIdFileId()),
            verification.getProviderExperienceYears(),
            verification.getProviderIntro(),
            fromJson(verification.getProviderServicePetTypes(), STRING_LIST_TYPE),
            verification.getProviderServiceCityCode(),
            fromJson(verification.getProviderCapabilityTags(), STRING_LIST_TYPE),
            resolveSupportingFileUrls(verification.getSupportingFileIds()),
            verification.getRejectReasonCode(),
            verification.getRejectReasonText(),
            reviewedByAdminName,
            verification.getReviewedAt(),
            verification.getCreatedAt(),
            verification.getUpdatedAt()
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_CS')")
    @Transactional
    public void approve(Long verificationId, ApproveVerificationRequest request) {
        long adminId = SecurityUtils.currentAdminId();
        UserVerification verification = userVerificationRepository.findById(verificationId)
            .orElseThrow(() -> new BizException(ErrorCode.VERIFICATION_NOT_FOUND, "Verification not found"));

        if (verification.getStatus() != VerificationStatus.PENDING) {
            throw new BizException(ErrorCode.VERIFICATION_REVIEW_NOT_ALLOWED, "Only pending verification can be approved");
        }

        Map<String, Object> before = auditSnapshot(verification);

        verification.setStatus(VerificationStatus.APPROVED);
        verification.setReviewedByAdminId(adminId);
        verification.setReviewedAt(LocalDateTime.now());
        verification.setRejectReasonCode(null);
        verification.setRejectReasonText(null);
        userVerificationRepository.save(verification);

        UserProfile profile = ensureUserProfile(verification.getUserId());
        if (verification.getVerificationType() == VerificationType.REAL_NAME) {
            profile.setRealNameVerified(true);
        }
        if (verification.getVerificationType() == VerificationType.PROVIDER) {
            profile.setProviderVerified(true);
        }
        userProfileRepository.save(profile);

        adminAuditService.writeAuditLog(
            adminId,
            "APPROVE_VERIFICATION",
            "USER_VERIFICATION",
            String.valueOf(verificationId),
            before,
            auditSnapshot(verification),
            request != null ? request.getRemark() : null
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_CS')")
    @Transactional
    public void reject(Long verificationId, RejectVerificationRequest request) {
        long adminId = SecurityUtils.currentAdminId();
        UserVerification verification = userVerificationRepository.findById(verificationId)
            .orElseThrow(() -> new BizException(ErrorCode.VERIFICATION_NOT_FOUND, "Verification not found"));

        if (verification.getStatus() != VerificationStatus.PENDING) {
            throw new BizException(ErrorCode.VERIFICATION_REVIEW_NOT_ALLOWED, "Only pending verification can be rejected");
        }

        Map<String, Object> before = auditSnapshot(verification);

        verification.setStatus(VerificationStatus.REJECTED);
        verification.setRejectReasonCode(request.getRejectReasonCode().trim());
        verification.setRejectReasonText(request.getRejectReasonText().trim());
        verification.setReviewedByAdminId(adminId);
        verification.setReviewedAt(LocalDateTime.now());
        userVerificationRepository.save(verification);

        adminAuditService.writeAuditLog(
            adminId,
            "REJECT_VERIFICATION",
            "USER_VERIFICATION",
            String.valueOf(verificationId),
            before,
            auditSnapshot(verification),
            request.getRemark()
        );
    }

    private Set<Long> resolveKeywordUserIds(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        String normalized = keyword.trim();
        Set<Long> userIds = new HashSet<>();

        if (normalized.matches("\\d+")) {
            try {
                userIds.add(Long.parseLong(normalized));
            } catch (NumberFormatException ignored) {
                // keep searching with other strategies
            }
        }

        userIds.addAll(userRepository.searchIdsByMobileOrEmail(normalized));
        userIds.addAll(userProfileRepository.searchUserIdsByNickname(normalized));

        return userIds;
    }

    private VerificationType parseVerificationType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return VerificationType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BizException(ErrorCode.INVALID_PARAM, "verificationType is invalid");
        }
    }

    private VerificationStatus parseVerificationStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return VerificationStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BizException(ErrorCode.INVALID_PARAM, "status is invalid");
        }
    }

    private UserProfile ensureUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId).orElseGet(() -> {
            UserProfile profile = new UserProfile();
            profile.setUserId(userId);
            profile.setNickname("用户" + userId);
            return profile;
        });
    }

    private String resolveFileUrl(Long fileId) {
        if (fileId == null) {
            return null;
        }
        return fileObjectRepository.findById(fileId).map(FileObject::getPublicUrl).orElse(null);
    }

    private List<String> resolveSupportingFileUrls(String jsonFileIds) {
        List<Long> fileIds = fromJson(jsonFileIds, LONG_LIST_TYPE);
        if (fileIds == null || fileIds.isEmpty()) {
            return null;
        }
        Map<Long, String> urlMap = fileObjectRepository.findAllById(fileIds).stream()
            .collect(Collectors.toMap(FileObject::getId, FileObject::getPublicUrl));

        return fileIds.stream()
            .map(urlMap::get)
            .filter(Objects::nonNull)
            .toList();
    }

    private Map<String, Object> auditSnapshot(UserVerification verification) {
        Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        snapshot.put("id", verification.getId());
        snapshot.put("status", verification.getStatus().name());
        snapshot.put("submitVersion", verification.getSubmitVersion());
        snapshot.put("verificationType", verification.getVerificationType().name());
        snapshot.put("rejectReasonCode", verification.getRejectReasonCode());
        snapshot.put("reviewedByAdminId", verification.getReviewedByAdminId());
        return snapshot;
    }

    private <T> List<T> fromJson(String json, TypeReference<List<T>> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
