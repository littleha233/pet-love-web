package com.petlove.weblove.modules.ops.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.ops.dto.admin.AdminAuditLogDetailDTO;
import com.petlove.weblove.modules.ops.dto.admin.AdminAuditLogListItemDTO;
import com.petlove.weblove.modules.ops.dto.admin.AdminAuditLogQuery;
import com.petlove.weblove.modules.ops.service.AuditLogQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/ops/audit-logs")
public class OpsAdminAuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    public OpsAdminAuditLogController(AuditLogQueryService auditLogQueryService) {
        this.auditLogQueryService = auditLogQueryService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminAuditLogListItemDTO>> list(AdminAuditLogQuery query) {
        return ApiResponse.success(auditLogQueryService.list(query));
    }

    @GetMapping("/{auditLogId}")
    public ApiResponse<AdminAuditLogDetailDTO> detail(@PathVariable Long auditLogId) {
        return ApiResponse.success(auditLogQueryService.detail(auditLogId));
    }
}
