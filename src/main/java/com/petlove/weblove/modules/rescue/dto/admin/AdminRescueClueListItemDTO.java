package com.petlove.weblove.modules.rescue.dto.admin;

import java.time.LocalDateTime;

public record AdminRescueClueListItemDTO(
    Long clueId,
    String clueNo,
    String cityName,
    String districtName,
    String petType,
    String urgencyLevel,
    String status,
    Long reporterUserId,
    String handledByAdminName,
    LocalDateTime handledAt,
    LocalDateTime createdAt
) {
}
