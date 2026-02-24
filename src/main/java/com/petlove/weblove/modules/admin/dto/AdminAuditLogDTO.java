package com.petlove.weblove.modules.admin.dto;

import java.time.LocalDateTime;

public record AdminAuditLogDTO(
    long id,
    long adminUserId,
    String adminDisplayName,
    String action,
    String targetType,
    String targetId,
    String remark,
    String requestId,
    LocalDateTime createdAt
) {
}
