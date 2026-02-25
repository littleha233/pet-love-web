package com.petlove.weblove.modules.rescue.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueClueDetailDTO;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueClueListItemDTO;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueClueQuery;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueGuideDetailDTO;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueGuideListItemDTO;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueGuideQuery;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueResourceDetailDTO;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueResourceListItemDTO;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueResourceQuery;
import com.petlove.weblove.modules.rescue.dto.admin.UpdateRescueClueStatusRequest;
import com.petlove.weblove.modules.rescue.dto.admin.UpsertRescueGuideRequest;
import com.petlove.weblove.modules.rescue.dto.admin.UpsertRescueResourceRequest;
import com.petlove.weblove.modules.rescue.service.AdminRescueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/rescue")
public class AdminRescueController {

    private final AdminRescueService adminRescueService;

    public AdminRescueController(AdminRescueService adminRescueService) {
        this.adminRescueService = adminRescueService;
    }

    @GetMapping("/guides")
    public ApiResponse<PageResponse<AdminRescueGuideListItemDTO>> listGuides(AdminRescueGuideQuery query) {
        return ApiResponse.success(adminRescueService.listGuides(query));
    }

    @GetMapping("/guides/{guideId}")
    public ApiResponse<AdminRescueGuideDetailDTO> guideDetail(@PathVariable Long guideId) {
        return ApiResponse.success(adminRescueService.guideDetail(guideId));
    }

    @PostMapping("/guides")
    public ApiResponse<AdminRescueGuideDetailDTO> createGuide(@Valid @RequestBody UpsertRescueGuideRequest request) {
        return ApiResponse.success(adminRescueService.createGuide(request));
    }

    @PutMapping("/guides/{guideId}")
    public ApiResponse<AdminRescueGuideDetailDTO> upsertGuide(@PathVariable Long guideId,
                                                               @Valid @RequestBody UpsertRescueGuideRequest request) {
        return ApiResponse.success(adminRescueService.upsertGuide(guideId, request));
    }

    @PostMapping("/guides/{guideId}/publish")
    public ApiResponse<Void> publishGuide(@PathVariable Long guideId) {
        adminRescueService.publishGuide(guideId);
        return ApiResponse.success();
    }

    @PostMapping("/guides/{guideId}/offline")
    public ApiResponse<Void> offlineGuide(@PathVariable Long guideId) {
        adminRescueService.offlineGuide(guideId);
        return ApiResponse.success();
    }

    @GetMapping("/resources")
    public ApiResponse<PageResponse<AdminRescueResourceListItemDTO>> listResources(AdminRescueResourceQuery query) {
        return ApiResponse.success(adminRescueService.listResources(query));
    }

    @GetMapping("/resources/{resourceId}")
    public ApiResponse<AdminRescueResourceDetailDTO> resourceDetail(@PathVariable Long resourceId) {
        return ApiResponse.success(adminRescueService.resourceDetail(resourceId));
    }

    @PostMapping("/resources")
    public ApiResponse<AdminRescueResourceDetailDTO> createResource(@Valid @RequestBody UpsertRescueResourceRequest request) {
        return ApiResponse.success(adminRescueService.createResource(request));
    }

    @PutMapping("/resources/{resourceId}")
    public ApiResponse<AdminRescueResourceDetailDTO> upsertResource(@PathVariable Long resourceId,
                                                                     @Valid @RequestBody UpsertRescueResourceRequest request) {
        return ApiResponse.success(adminRescueService.upsertResource(resourceId, request));
    }

    @PostMapping("/resources/{resourceId}/activate")
    public ApiResponse<Void> activateResource(@PathVariable Long resourceId) {
        adminRescueService.activateResource(resourceId);
        return ApiResponse.success();
    }

    @PostMapping("/resources/{resourceId}/pause")
    public ApiResponse<Void> pauseResource(@PathVariable Long resourceId) {
        adminRescueService.pauseResource(resourceId);
        return ApiResponse.success();
    }

    @PostMapping("/resources/{resourceId}/offline")
    public ApiResponse<Void> offlineResource(@PathVariable Long resourceId) {
        adminRescueService.offlineResource(resourceId);
        return ApiResponse.success();
    }

    @GetMapping("/clues")
    public ApiResponse<PageResponse<AdminRescueClueListItemDTO>> listClues(AdminRescueClueQuery query) {
        return ApiResponse.success(adminRescueService.listClues(query));
    }

    @GetMapping("/clues/{clueId}")
    public ApiResponse<AdminRescueClueDetailDTO> clueDetail(@PathVariable Long clueId) {
        return ApiResponse.success(adminRescueService.clueDetail(clueId));
    }

    @PostMapping("/clues/{clueId}/status")
    public ApiResponse<AdminRescueClueDetailDTO> updateClueStatus(@PathVariable Long clueId,
                                                                   @Valid @RequestBody UpdateRescueClueStatusRequest request) {
        return ApiResponse.success(adminRescueService.updateClueStatus(clueId, request));
    }
}
