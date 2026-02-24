package com.petlove.weblove.modules.verification.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

public record AdminVerificationDetailDTO(
    long id,
    long userId,
    String nickname,
    String mobileMasked,
    String emailMasked,
    String verificationType,
    String status,
    int submitVersion,
    String realName,
    String idNoMasked,
    String idFrontUrl,
    String idBackUrl,
    String holdingIdUrl,
    Integer providerExperienceYears,
    String providerIntro,
    List<String> providerServicePetTypes,
    String providerServiceCityCode,
    List<String> providerCapabilityTags,
    List<String> supportingFileUrls,
    String rejectReasonCode,
    String rejectReasonText,
    String reviewedByAdminName,
    LocalDateTime reviewedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
