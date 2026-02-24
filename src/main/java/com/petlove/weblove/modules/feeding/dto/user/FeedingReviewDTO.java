package com.petlove.weblove.modules.feeding.dto.user;

import java.time.LocalDateTime;

public record FeedingReviewDTO(
    Long reviewId,
    Long orderId,
    Integer ratingOverall,
    Integer ratingTimeliness,
    Integer ratingCleanliness,
    Integer ratingAttitude,
    String content,
    LocalDateTime createdAt
) {
}
