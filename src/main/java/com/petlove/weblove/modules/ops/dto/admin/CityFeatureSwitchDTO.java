package com.petlove.weblove.modules.ops.dto.admin;

import java.time.LocalDateTime;

public record CityFeatureSwitchDTO(
    Long switchId,
    String cityCode,
    String cityName,
    String featureKey,
    Boolean isEnabled,
    Boolean allowRead,
    Boolean allowWrite,
    String noticeText,
    LocalDateTime effectiveFrom,
    LocalDateTime effectiveTo,
    LocalDateTime updatedAt
) {
}
