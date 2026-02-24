package com.petlove.weblove.modules.adoption.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.adoption.dto.admin.AdminAdoptionPostDetailDTO;
import com.petlove.weblove.modules.adoption.dto.admin.AdminAdoptionPostListItemDTO;
import com.petlove.weblove.modules.adoption.dto.admin.AdminAdoptionPostQuery;
import com.petlove.weblove.modules.adoption.dto.admin.ApproveAdoptionPostRequest;
import com.petlove.weblove.modules.adoption.dto.admin.OfflineAdoptionPostRequest;
import com.petlove.weblove.modules.adoption.dto.admin.RejectAdoptionPostRequest;
import com.petlove.weblove.modules.adoption.service.AdminAdoptionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/adoptions/posts")
public class AdminAdoptionController {

    private final AdminAdoptionService adminAdoptionService;

    public AdminAdoptionController(AdminAdoptionService adminAdoptionService) {
        this.adminAdoptionService = adminAdoptionService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminAdoptionPostListItemDTO>> list(AdminAdoptionPostQuery query) {
        return ApiResponse.success(adminAdoptionService.list(query));
    }

    @GetMapping("/{postId}")
    public ApiResponse<AdminAdoptionPostDetailDTO> detail(@PathVariable Long postId) {
        return ApiResponse.success(adminAdoptionService.detail(postId));
    }

    @PostMapping("/{postId}/approve")
    public ApiResponse<Void> approve(@PathVariable Long postId,
                                     @Valid @RequestBody(required = false) ApproveAdoptionPostRequest request) {
        adminAdoptionService.approve(postId, request == null ? new ApproveAdoptionPostRequest() : request);
        return ApiResponse.success();
    }

    @PostMapping("/{postId}/reject")
    public ApiResponse<Void> reject(@PathVariable Long postId,
                                    @Valid @RequestBody RejectAdoptionPostRequest request) {
        adminAdoptionService.reject(postId, request);
        return ApiResponse.success();
    }

    @PostMapping("/{postId}/offline")
    public ApiResponse<Void> offline(@PathVariable Long postId,
                                     @Valid @RequestBody OfflineAdoptionPostRequest request) {
        adminAdoptionService.offline(postId, request);
        return ApiResponse.success();
    }
}
