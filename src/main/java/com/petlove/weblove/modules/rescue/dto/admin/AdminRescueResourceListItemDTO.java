package com.petlove.weblove.modules.rescue.dto.admin;

import java.time.LocalDateTime;

public record AdminRescueResourceListItemDTO(
    Long resourceId,
    String resourceType,
    String name,
    String cityName,
    String status,
    LocalDateTime verifiedAt,
    LocalDateTime updatedAt
) {
}
