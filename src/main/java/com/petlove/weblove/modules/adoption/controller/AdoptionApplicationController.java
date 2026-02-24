package com.petlove.weblove.modules.adoption.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.adoption.dto.user.AdoptionApplicationDTO;
import com.petlove.weblove.modules.adoption.dto.user.HandleAdoptionApplicationRequest;
import com.petlove.weblove.modules.adoption.dto.user.MyAdoptionApplicationListItemDTO;
import com.petlove.weblove.modules.adoption.dto.user.SubmitAdoptionApplicationRequest;
import com.petlove.weblove.modules.adoption.service.AdoptionApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/adoptions")
public class AdoptionApplicationController {

    private final AdoptionApplicationService adoptionApplicationService;

    public AdoptionApplicationController(AdoptionApplicationService adoptionApplicationService) {
        this.adoptionApplicationService = adoptionApplicationService;
    }

    @PostMapping("/posts/{postId}/applications")
    public ApiResponse<AdoptionApplicationDTO> submit(@PathVariable Long postId,
                                                      @Valid @RequestBody SubmitAdoptionApplicationRequest request) {
        return ApiResponse.success(adoptionApplicationService.submit(postId, request));
    }

    @GetMapping("/my/applications")
    public ApiResponse<PageResponse<MyAdoptionApplicationListItemDTO>> myApplications(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(adoptionApplicationService.myApplications(page, pageSize, status));
    }

    @GetMapping("/posts/{postId}/applications")
    public ApiResponse<PageResponse<AdoptionApplicationDTO>> postApplications(
        @PathVariable Long postId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.success(adoptionApplicationService.postApplications(postId, page, pageSize));
    }

    @PostMapping("/applications/{applicationId}/handle")
    public ApiResponse<AdoptionApplicationDTO> handle(@PathVariable Long applicationId,
                                                      @Valid @RequestBody HandleAdoptionApplicationRequest request) {
        return ApiResponse.success(adoptionApplicationService.handle(applicationId, request));
    }

    @PostMapping("/applications/{applicationId}/withdraw")
    public ApiResponse<AdoptionApplicationDTO> withdraw(@PathVariable Long applicationId) {
        return ApiResponse.success(adoptionApplicationService.withdraw(applicationId));
    }
}
