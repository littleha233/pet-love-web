package com.petlove.weblove.modules.rescue.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.rescue.dto.user.RescueResourceDetailDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueResourceListItemDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueResourceListQuery;
import com.petlove.weblove.modules.rescue.service.RescueResourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rescue/resources")
public class RescueResourceController {

    private final RescueResourceService rescueResourceService;

    public RescueResourceController(RescueResourceService rescueResourceService) {
        this.rescueResourceService = rescueResourceService;
    }

    @GetMapping
    public ApiResponse<PageResponse<RescueResourceListItemDTO>> list(RescueResourceListQuery query) {
        return ApiResponse.success(rescueResourceService.list(query));
    }

    @GetMapping("/{resourceId}")
    public ApiResponse<RescueResourceDetailDTO> detail(@PathVariable Long resourceId) {
        return ApiResponse.success(rescueResourceService.detail(resourceId));
    }
}
