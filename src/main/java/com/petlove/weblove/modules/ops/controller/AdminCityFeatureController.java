package com.petlove.weblove.modules.ops.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.ops.dto.admin.CityFeatureSwitchDTO;
import com.petlove.weblove.modules.ops.dto.admin.CityFeatureSwitchQuery;
import com.petlove.weblove.modules.ops.dto.admin.UpsertCityFeatureSwitchRequest;
import com.petlove.weblove.modules.ops.service.CityFeatureSwitchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/ops/city-features")
public class AdminCityFeatureController {

    private final CityFeatureSwitchService cityFeatureSwitchService;

    public AdminCityFeatureController(CityFeatureSwitchService cityFeatureSwitchService) {
        this.cityFeatureSwitchService = cityFeatureSwitchService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CityFeatureSwitchDTO>> list(CityFeatureSwitchQuery query) {
        return ApiResponse.success(cityFeatureSwitchService.list(query));
    }

    @PutMapping("/{switchId}")
    public ApiResponse<CityFeatureSwitchDTO> upsert(@PathVariable Long switchId,
                                                     @Valid @RequestBody UpsertCityFeatureSwitchRequest request) {
        return ApiResponse.success(cityFeatureSwitchService.upsert(switchId, request));
    }
}
