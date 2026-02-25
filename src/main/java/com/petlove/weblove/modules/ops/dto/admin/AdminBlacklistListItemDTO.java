package com.petlove.weblove.modules.ops.dto.admin;

import java.time.LocalDateTime;

public record AdminBlacklistListItemDTO(
    Long blacklistId,
    String subjectType,
    String subjectValueMasked,
    String scopeType,
    String scopeValue,
    String actionMode,
    String reasonCode,
    String status,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime updatedAt
) {
}
