package com.petlove.weblove.modules.verification.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.verification.dto.admin.AdminVerificationDetailDTO;
import com.petlove.weblove.modules.verification.dto.admin.AdminVerificationListItemDTO;
import com.petlove.weblove.modules.verification.dto.admin.AdminVerificationQuery;
import com.petlove.weblove.modules.verification.dto.admin.ApproveVerificationRequest;
import com.petlove.weblove.modules.verification.dto.admin.RejectVerificationRequest;
import com.petlove.weblove.modules.verification.service.AdminVerificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/verifications")
public class AdminVerificationController {

    private final AdminVerificationService adminVerificationService;

    public AdminVerificationController(AdminVerificationService adminVerificationService) {
        this.adminVerificationService = adminVerificationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminVerificationListItemDTO>> list(AdminVerificationQuery query) {
        return ApiResponse.success(adminVerificationService.list(query));
    }

    @GetMapping("/{verificationId}")
    public ApiResponse<AdminVerificationDetailDTO> detail(@PathVariable Long verificationId) {
        return ApiResponse.success(adminVerificationService.detail(verificationId));
    }

    @PostMapping("/{verificationId}/approve")
    public ApiResponse<Void> approve(@PathVariable Long verificationId,
                                     @Valid @RequestBody(required = false) ApproveVerificationRequest request) {
        adminVerificationService.approve(verificationId, request == null ? new ApproveVerificationRequest() : request);
        return ApiResponse.success();
    }

    @PostMapping("/{verificationId}/reject")
    public ApiResponse<Void> reject(@PathVariable Long verificationId,
                                    @Valid @RequestBody RejectVerificationRequest request) {
        adminVerificationService.reject(verificationId, request);
        return ApiResponse.success();
    }
}
