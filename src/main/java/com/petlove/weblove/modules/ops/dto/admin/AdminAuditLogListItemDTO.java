package com.petlove.weblove.modules.ops.dto.admin;

import java.time.LocalDateTime;

public record AdminAuditLogListItemDTO(
    Long auditLogId,
    Long operatorAdminId,
    String operatorAdminName,
    String moduleName,
    String actionName,
    String targetType,
    String targetId,
    String summary,
    LocalDateTime createdAt
) {
}
