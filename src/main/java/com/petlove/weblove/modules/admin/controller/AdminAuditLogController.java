package com.petlove.weblove.modules.admin.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.admin.dto.AdminAuditLogDTO;
import com.petlove.weblove.modules.admin.service.AdminAuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/audit-logs")
public class AdminAuditLogController {

    private final AdminAuditService adminAuditService;

    public AdminAuditLogController(AdminAuditService adminAuditService) {
        this.adminAuditService = adminAuditService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminAuditLogDTO>> list(
        @RequestParam(required = false) Long adminUserId,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String targetType,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(pageSize, 1), 100),
            Sort.by(Sort.Direction.DESC, "id"));
        Page<AdminAuditLogDTO> dtoPage = adminAuditService.list(adminUserId, action, targetType, pageable);

        return ApiResponse.success(new PageResponse<>(
            dtoPage.getContent(),
            page,
            pageSize,
            dtoPage.getTotalElements()
        ));
    }
}
