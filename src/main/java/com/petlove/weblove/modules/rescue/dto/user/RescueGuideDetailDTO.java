package com.petlove.weblove.modules.rescue.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public record RescueGuideDetailDTO(
    Long guideId,
    String scenarioCode,
    String title,
    String summary,
    String contentMd,
    String cityCode,
    List<String> tags,
    LocalDateTime publishedAt,
    LocalDateTime updatedAt
) {
}
