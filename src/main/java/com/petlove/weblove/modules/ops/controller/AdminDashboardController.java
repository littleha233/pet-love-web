package com.petlove.weblove.modules.ops.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.modules.ops.dto.admin.DashboardSummaryDTO;
import com.petlove.weblove.modules.ops.dto.admin.DashboardTodoItemDTO;
import com.petlove.weblove.modules.ops.dto.admin.DashboardTrendPointDTO;
import com.petlove.weblove.modules.ops.dto.admin.DashboardTrendQuery;
import com.petlove.weblove.modules.ops.service.DashboardService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/ops/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryDTO> summary() {
        return ApiResponse.success(dashboardService.summary());
    }

    @GetMapping("/todos")
    public ApiResponse<List<DashboardTodoItemDTO>> todos() {
        return ApiResponse.success(dashboardService.todos());
    }

    @GetMapping("/trends")
    public ApiResponse<List<DashboardTrendPointDTO>> trends(@Valid DashboardTrendQuery query) {
        return ApiResponse.success(dashboardService.trends(query));
    }
}
