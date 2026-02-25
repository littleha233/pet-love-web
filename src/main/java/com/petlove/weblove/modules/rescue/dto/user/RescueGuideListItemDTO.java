package com.petlove.weblove.modules.rescue.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public record RescueGuideListItemDTO(
    Long guideId,
    String scenarioCode,
    String title,
    String summary,
    String cityCode,
    List<String> tags,
    LocalDateTime publishedAt,
    Integer sortOrder
) {
}
