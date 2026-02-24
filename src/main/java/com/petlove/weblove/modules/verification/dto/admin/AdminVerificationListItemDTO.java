package com.petlove.weblove.modules.verification.dto.admin;

import java.time.LocalDateTime;

public record AdminVerificationListItemDTO(
    long id,
    long userId,
    String nickname,
    String mobileMasked,
    String verificationType,
    String status,
    int submitVersion,
    String providerServiceCityCode,
    String reviewedByAdminName,
    LocalDateTime reviewedAt,
    LocalDateTime updatedAt
) {
}
