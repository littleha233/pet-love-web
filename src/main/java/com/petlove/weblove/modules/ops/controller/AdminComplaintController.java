package com.petlove.weblove.modules.ops.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.ops.dto.admin.AdminComplaintTicketDetailDTO;
import com.petlove.weblove.modules.ops.dto.admin.AdminComplaintTicketListItemDTO;
import com.petlove.weblove.modules.ops.dto.admin.AdminComplaintTicketQuery;
import com.petlove.weblove.modules.ops.dto.admin.AdminReplyComplaintTicketRequest;
import com.petlove.weblove.modules.ops.dto.admin.UpdateComplaintTicketStatusRequest;
import com.petlove.weblove.modules.ops.service.AdminComplaintService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/ops/complaints")
public class AdminComplaintController {

    private final AdminComplaintService adminComplaintService;

    public AdminComplaintController(AdminComplaintService adminComplaintService) {
        this.adminComplaintService = adminComplaintService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminComplaintTicketListItemDTO>> list(AdminComplaintTicketQuery query) {
        return ApiResponse.success(adminComplaintService.list(query));
    }

    @GetMapping("/{ticketId}")
    public ApiResponse<AdminComplaintTicketDetailDTO> detail(@PathVariable Long ticketId) {
        return ApiResponse.success(adminComplaintService.detail(ticketId));
    }

    @PostMapping("/{ticketId}/reply")
    public ApiResponse<AdminComplaintTicketDetailDTO> reply(@PathVariable Long ticketId,
                                                             @Valid @RequestBody AdminReplyComplaintTicketRequest request) {
        return ApiResponse.success(adminComplaintService.reply(ticketId, request));
    }

    @PostMapping("/{ticketId}/status")
    public ApiResponse<AdminComplaintTicketDetailDTO> updateStatus(@PathVariable Long ticketId,
                                                                    @Valid @RequestBody UpdateComplaintTicketStatusRequest request) {
        return ApiResponse.success(adminComplaintService.updateStatus(ticketId, request));
    }
}
