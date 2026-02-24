package com.petlove.weblove.modules.adoption.dto.user;

import java.time.LocalDateTime;

public record MyRehomePostListItemDTO(
    long postId,
    String title,
    String status,
    String coverImageUrl,
    String cityName,
    int applicationCount,
    String rejectReasonText,
    LocalDateTime updatedAt,
    LocalDateTime publishedAt
) {
}
