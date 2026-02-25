package com.petlove.weblove.modules.ops.dto.user;

import java.time.LocalDateTime;

public record ComplaintTicketListItemDTO(
    Long ticketId,
    String ticketNo,
    String targetType,
    String title,
    String priority,
    String status,
    LocalDateTime lastReplyAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
