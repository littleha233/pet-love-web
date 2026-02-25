package com.petlove.weblove.modules.rescue.dto.user;

import java.time.LocalDateTime;

public record RescueClueListItemDTO(
    Long clueId,
    String clueNo,
    String cityName,
    String districtName,
    String petType,
    String urgencyLevel,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
