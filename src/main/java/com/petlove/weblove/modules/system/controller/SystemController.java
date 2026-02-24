package com.petlove.weblove.modules.system.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.modules.system.dto.HealthDTO;
import com.petlove.weblove.modules.system.service.SystemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/health")
    public ApiResponse<HealthDTO> health() {
        return ApiResponse.success(systemService.health());
    }

    @GetMapping("/ready")
    public ApiResponse<HealthDTO> ready() {
        return ApiResponse.success(systemService.ready());
    }
}
