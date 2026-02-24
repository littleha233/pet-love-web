package com.petlove.weblove.modules.verification.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public record VerificationDetailDTO(
    String verificationType,
    String status,
    int submitVersion,
    String realName,
    String idNoMasked,
    Long idFrontFileId,
    String idFrontUrl,
    Long idBackFileId,
    String idBackUrl,
    Long holdingIdFileId,
    String holdingIdUrl,
    Integer providerExperienceYears,
    String providerIntro,
    List<String> providerServicePetTypes,
    String providerServiceCityCode,
    List<String> providerCapabilityTags,
    String rejectReasonCode,
    String rejectReasonText,
    LocalDateTime reviewedAt,
    LocalDateTime updatedAt
) {
}
