package com.petlove.weblove.modules.adoption.dto.admin;

import java.time.LocalDateTime;

public record AdminAdoptionPostListItemDTO(
    long postId,
    String title,
    String status,
    long publisherUserId,
    String publisherNickname,
    String cityName,
    String petType,
    int submitVersion,
    String reviewedByAdminName,
    LocalDateTime reviewedAt,
    LocalDateTime updatedAt
) {
}
