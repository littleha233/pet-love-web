package com.petlove.weblove.modules.rescue.dto.admin;

import java.time.LocalDateTime;

public record AdminRescueGuideListItemDTO(
    Long guideId,
    String scenarioCode,
    String title,
    String cityCode,
    String status,
    Integer version,
    LocalDateTime publishedAt,
    LocalDateTime updatedAt
) {
}
