package com.petlove.weblove.modules.adoption.dto.user;

import java.time.LocalDateTime;

public record RehomePostDetailDTO(
    long postId,
    String title,
    String content,
    String status,
    String cityCode,
    String cityName,
    String districtName,
    RehomePostPublisherDTO publisher,
    RehomePostPetDTO pet,
    LocalDateTime publishedAt,
    int viewCount,
    ApplicationStatsDTO applicationStats,
    ViewerContextDTO viewerContext
) {
}
