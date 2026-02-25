package com.petlove.weblove.modules.rescue.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.rescue.dto.user.RescueGuideDetailDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueGuideListItemDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueGuideListQuery;
import com.petlove.weblove.modules.rescue.service.RescueGuideService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rescue/guides")
public class RescueGuideController {

    private final RescueGuideService rescueGuideService;

    public RescueGuideController(RescueGuideService rescueGuideService) {
        this.rescueGuideService = rescueGuideService;
    }

    @GetMapping
    public ApiResponse<PageResponse<RescueGuideListItemDTO>> list(RescueGuideListQuery query) {
        return ApiResponse.success(rescueGuideService.list(query));
    }

    @GetMapping("/{guideId}")
    public ApiResponse<RescueGuideDetailDTO> detail(@PathVariable Long guideId) {
        return ApiResponse.success(rescueGuideService.detail(guideId));
    }
}
