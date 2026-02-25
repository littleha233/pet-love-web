package com.petlove.weblove.modules.rescue.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

public record AdminRescueGuideDetailDTO(
    Long guideId,
    String scenarioCode,
    String title,
    String summary,
    String contentMd,
    String cityCode,
    List<String> tags,
    Integer sortOrder,
    String status,
    Integer version,
    LocalDateTime publishedAt,
    Long createdByAdminId,
    Long updatedByAdminId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
