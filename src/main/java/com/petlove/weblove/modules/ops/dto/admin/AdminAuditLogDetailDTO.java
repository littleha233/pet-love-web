package com.petlove.weblove.modules.ops.dto.admin;

import java.time.LocalDateTime;

public record AdminAuditLogDetailDTO(
    Long auditLogId,
    Long operatorAdminId,
    String operatorAdminName,
    String moduleName,
    String actionName,
    String targetType,
    String targetId,
    Object beforeSnapshot,
    Object afterSnapshot,
    Object extraData,
    LocalDateTime createdAt
) {
}
