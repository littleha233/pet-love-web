package com.petlove.weblove.modules.system.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.modules.system.dto.CityDTO;
import com.petlove.weblove.modules.system.service.SystemService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meta")
public class MetaController {

    private final SystemService systemService;

    public MetaController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/cities")
    public ApiResponse<List<CityDTO>> cities() {
        return ApiResponse.success(systemService.cities());
    }
}
