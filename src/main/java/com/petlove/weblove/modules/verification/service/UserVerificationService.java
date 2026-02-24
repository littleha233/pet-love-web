package com.petlove.weblove.modules.verification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.HashUtil;
import com.petlove.weblove.common.util.MaskUtil;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.enums.FileBizType;
import com.petlove.weblove.modules.file.enums.FileStatus;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.system.entity.City;
import com.petlove.weblove.modules.system.repository.CityRepository;
import com.petlove.weblove.modules.verification.dto.user.MyVerificationOverviewDTO;
import com.petlove.weblove.modules.verification.dto.user.SubmitProviderVerificationRequest;
import com.petlove.weblove.modules.verification.dto.user.SubmitRealNameVerificationRequest;
import com.petlove.weblove.modules.verification.dto.user.VerificationDetailDTO;
import com.petlove.weblove.modules.verification.dto.user.VerificationStatusDTO;
import com.petlove.weblove.modules.verification.dto.user.VerificationStatusSummaryDTO;
import com.petlove.weblove.modules.verification.entity.UserVerification;
import com.petlove.weblove.modules.verification.enums.ProviderCapabilityTag;
import com.petlove.weblove.modules.verification.enums.ProviderPetType;
import com.petlove.weblove.modules.verification.enums.VerificationStatus;
import com.petlove.weblove.modules.verification.enums.VerificationType;
import com.petlove.weblove.modules.verification.repository.UserVerificationRepository;
import com.petlove.weblove.security.SecurityUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserVerificationService {

    private static final String NOT_SUBMITTED = "NOT_SUBMITTED";
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<Long>> LONG_LIST_TYPE = new TypeReference<>() {
    };
    private static final Set<FileBizType> REAL_NAME_FILE_BIZ_TYPES = EnumSet.of(FileBizType.ID_CARD);
    private static final Set<FileBizType> PROVIDER_SUPPORTING_FILE_BIZ_TYPES = EnumSet.of(FileBizType.CERTIFICATE, FileBizType.OTHER);

    private final UserVerificationRepository userVerificationRepository;
    private final FileObjectRepository fileObjectRepository;
    private final CityRepository cityRepository;
    private final ObjectMapper objectMapper;

    public UserVerificationService(UserVerificationRepository userVerificationRepository,
                                   FileObjectRepository fileObjectRepository,
                                   CityRepository cityRepository,
                                   ObjectMapper objectMapper) {
        this.userVerificationRepository = userVerificationRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.cityRepository = cityRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public MyVerificationOverviewDTO getMyOverview() {
        long userId = SecurityUtils.currentUserId();
        Map<VerificationType, UserVerification> verificationMap = userVerificationRepository.findByUserId(userId).stream()
            .collect(Collectors.toMap(UserVerification::getVerificationType, Function.identity()));

        UserVerification realName = verificationMap.get(VerificationType.REAL_NAME);
        UserVerification provider = verificationMap.get(VerificationType.PROVIDER);

        return new MyVerificationOverviewDTO(
            toSummary(realName),
            toSummary(provider),
            realName != null && realName.getStatus() == VerificationStatus.APPROVED
        );
    }

    @Transactional(readOnly = true)
    public VerificationDetailDTO getMyDetail(VerificationType type) {
        long userId = SecurityUtils.currentUserId();
        UserVerification verification = userVerificationRepository.findByUserIdAndVerificationType(userId, type)
            .orElseThrow(() -> new BizException(ErrorCode.VERIFICATION_NOT_FOUND, "Verification not found"));
        return toUserDetail(verification);
    }

    @Transactional
    public VerificationStatusDTO submitRealName(SubmitRealNameVerificationRequest request) {
        long userId = SecurityUtils.currentUserId();

        validateVerificationFile(request.getIdFrontFileId(), userId, REAL_NAME_FILE_BIZ_TYPES);
        validateVerificationFile(request.getIdBackFileId(), userId, REAL_NAME_FILE_BIZ_TYPES);
        if (request.getHoldingIdFileId() != null) {
            validateVerificationFile(request.getHoldingIdFileId(), userId, REAL_NAME_FILE_BIZ_TYPES);
        }

        UserVerification verification = prepareForSubmit(userId, VerificationType.REAL_NAME);
        verification.setRealName(request.getRealName().trim());
        verification.setIdNoMasked(MaskUtil.maskIdNo(request.getIdNo()));
        verification.setIdNoHash(HashUtil.sha256(request.getIdNo().trim()));
        verification.setIdFrontFileId(request.getIdFrontFileId());
        verification.setIdBackFileId(request.getIdBackFileId());
        verification.setHoldingIdFileId(request.getHoldingIdFileId());

        clearReviewResult(verification);
        UserVerification saved = userVerificationRepository.save(verification);
        return toStatus(saved);
    }

    @Transactional
    public VerificationStatusDTO submitProvider(SubmitProviderVerificationRequest request) {
        long userId = SecurityUtils.currentUserId();

        UserVerification realNameVerification = userVerificationRepository.findByUserIdAndVerificationType(userId, VerificationType.REAL_NAME)
            .orElseThrow(() -> new BizException(ErrorCode.VERIFICATION_REAL_NAME_REQUIRED,
                "Real-name verification must be approved before provider submission"));
        if (realNameVerification.getStatus() != VerificationStatus.APPROVED) {
            throw new BizException(ErrorCode.VERIFICATION_REAL_NAME_REQUIRED,
                "Real-name verification must be approved before provider submission");
        }

        City city = cityRepository.findByCityCode(request.getProviderServiceCityCode().trim())
            .orElseThrow(() -> new BizException(ErrorCode.INVALID_PARAM, "City code is invalid"));
        if (!city.isEnabled()) {
            throw new BizException(ErrorCode.INVALID_PARAM, "City is not enabled");
        }

        List<String> providerPetTypes = normalizeEnumValues(request.getProviderServicePetTypes(), ProviderPetType.class, "providerServicePetTypes");
        List<String> capabilityTags = request.getProviderCapabilityTags() == null
            ? null
            : normalizeEnumValues(request.getProviderCapabilityTags(), ProviderCapabilityTag.class, "providerCapabilityTags");
        List<Long> supportingFileIds = normalizeFileIds(request.getSupportingFileIds());

        if (supportingFileIds != null) {
            for (Long fileId : supportingFileIds) {
                validateVerificationFile(fileId, userId, PROVIDER_SUPPORTING_FILE_BIZ_TYPES);
            }
        }

        UserVerification verification = prepareForSubmit(userId, VerificationType.PROVIDER);
        verification.setProviderExperienceYears(request.getProviderExperienceYears());
        verification.setProviderIntro(request.getProviderIntro().trim());
        verification.setProviderServicePetTypes(toJson(providerPetTypes));
        verification.setProviderServiceCityCode(request.getProviderServiceCityCode().trim());
        verification.setProviderCapabilityTags(toJson(capabilityTags));
        verification.setSupportingFileIds(toJson(supportingFileIds));

        clearReviewResult(verification);
        UserVerification saved = userVerificationRepository.save(verification);
        return toStatus(saved);
    }

    private UserVerification prepareForSubmit(long userId, VerificationType verificationType) {
        UserVerification verification = userVerificationRepository.findByUserIdAndVerificationType(userId, verificationType)
            .orElseGet(() -> {
                UserVerification created = new UserVerification();
                created.setUserId(userId);
                created.setVerificationType(verificationType);
                created.setStatus(VerificationStatus.PENDING);
                created.setSubmitVersion(1);
                return created;
            });

        if (verification.getId() != null) {
            if (verification.getStatus() == VerificationStatus.APPROVED) {
                throw new BizException(ErrorCode.VERIFICATION_ALREADY_APPROVED, "Verification already approved");
            }
            if (verification.getStatus() == VerificationStatus.PENDING) {
                throw new BizException(ErrorCode.VERIFICATION_SUBMIT_NOT_ALLOWED, "Verification is already pending");
            }
            if (verification.getStatus() != VerificationStatus.REJECTED) {
                throw new BizException(ErrorCode.VERIFICATION_STATUS_INVALID, "Verification status does not allow submit");
            }
            verification.setSubmitVersion(verification.getSubmitVersion() + 1);
        }

        verification.setStatus(VerificationStatus.PENDING);
        return verification;
    }

    private void clearReviewResult(UserVerification verification) {
        verification.setRejectReasonCode(null);
        verification.setRejectReasonText(null);
        verification.setReviewedByAdminId(null);
        verification.setReviewedAt(null);
    }

    private void validateVerificationFile(Long fileId, long userId, Set<FileBizType> allowedBizTypes) {
        FileObject fileObject = fileObjectRepository.findById(fileId)
            .orElseThrow(() -> new BizException(ErrorCode.VERIFICATION_FILE_INVALID, "Verification file not found"));

        if (fileObject.getStatus() != FileStatus.READY || !allowedBizTypes.contains(fileObject.getBizType())) {
            throw new BizException(ErrorCode.VERIFICATION_FILE_INVALID, "Verification file is invalid");
        }

        if (!userIdEquals(fileObject.getOwnerUserId(), userId)) {
            throw new BizException(ErrorCode.VERIFICATION_FILE_NOT_OWNED, "Verification file is not owned by current user");
        }
    }

    private boolean userIdEquals(Long ownerUserId, long userId) {
        return ownerUserId != null && ownerUserId == userId;
    }

    private VerificationStatusSummaryDTO toSummary(UserVerification verification) {
        if (verification == null) {
            return new VerificationStatusSummaryDTO(NOT_SUBMITTED, null, null, null);
        }
        return new VerificationStatusSummaryDTO(
            verification.getStatus().name(),
            verification.getSubmitVersion(),
            verification.getRejectReasonText(),
            verification.getUpdatedAt()
        );
    }

    private VerificationStatusDTO toStatus(UserVerification verification) {
        return new VerificationStatusDTO(
            verification.getVerificationType().name(),
            verification.getStatus().name(),
            verification.getSubmitVersion(),
            verification.getUpdatedAt()
        );
    }

    private VerificationDetailDTO toUserDetail(UserVerification verification) {
        return new VerificationDetailDTO(
            verification.getVerificationType().name(),
            verification.getStatus().name(),
            verification.getSubmitVersion(),
            verification.getRealName(),
            verification.getIdNoMasked(),
            verification.getIdFrontFileId(),
            resolveFileUrl(verification.getIdFrontFileId()),
            verification.getIdBackFileId(),
            resolveFileUrl(verification.getIdBackFileId()),
            verification.getHoldingIdFileId(),
            resolveFileUrl(verification.getHoldingIdFileId()),
            verification.getProviderExperienceYears(),
            verification.getProviderIntro(),
            fromJson(verification.getProviderServicePetTypes(), STRING_LIST_TYPE),
            verification.getProviderServiceCityCode(),
            fromJson(verification.getProviderCapabilityTags(), STRING_LIST_TYPE),
            verification.getRejectReasonCode(),
            verification.getRejectReasonText(),
            verification.getReviewedAt(),
            verification.getUpdatedAt()
        );
    }

    private String resolveFileUrl(Long fileId) {
        if (fileId == null) {
            return null;
        }
        return fileObjectRepository.findById(fileId).map(FileObject::getPublicUrl).orElse(null);
    }

    private <E extends Enum<E>> List<String> normalizeEnumValues(List<String> values, Class<E> enumClass, String fieldName) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> normalized = new ArrayList<>();
        for (String raw : values) {
            if (raw == null || raw.isBlank()) {
                throw new BizException(ErrorCode.INVALID_PARAM, fieldName + " contains blank value");
            }
            String upperValue = raw.trim().toUpperCase(Locale.ROOT);
            try {
                E enumValue = Enum.valueOf(enumClass, upperValue);
                normalized.add(enumValue.name());
            } catch (IllegalArgumentException e) {
                throw new BizException(ErrorCode.INVALID_PARAM, fieldName + " contains invalid value: " + raw);
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(normalized));
    }

    private List<Long> normalizeFileIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        List<Long> normalized = ids.stream()
            .filter(id -> id != null && id > 0)
            .distinct()
            .toList();
        if (normalized.size() != ids.size()) {
            throw new BizException(ErrorCode.INVALID_PARAM, "supportingFileIds contains invalid value");
        }
        return normalized;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to serialize verification field");
        }
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
