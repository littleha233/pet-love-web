package com.petlove.weblove.modules.ops.dto.admin;

import java.time.LocalDateTime;

public record AdminComplaintTicketListItemDTO(
    Long ticketId,
    String ticketNo,
    Long reporterUserId,
    String targetType,
    String title,
    String priority,
    String status,
    String assignedAdminName,
    LocalDateTime lastReplyAt,
    LocalDateTime createdAt
) {
}
