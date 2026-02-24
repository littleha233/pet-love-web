package com.petlove.weblove.modules.adoption.dto.user;

import java.time.LocalDateTime;

public record MyAdoptionApplicationListItemDTO(
    long applicationId,
    long postId,
    String postTitle,
    String postCoverImageUrl,
    String status,
    String cityName,
    LocalDateTime createdAt,
    LocalDateTime handledAt
) {
}
