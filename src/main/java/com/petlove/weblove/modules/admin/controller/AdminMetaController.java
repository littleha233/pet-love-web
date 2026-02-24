package com.petlove.weblove.modules.admin.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.modules.admin.dto.AdminMenuItemDTO;
import com.petlove.weblove.modules.admin.service.AdminAuthService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/meta")
public class AdminMetaController {

    private final AdminAuthService adminAuthService;

    public AdminMetaController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/menus")
    public ApiResponse<List<AdminMenuItemDTO>> menus() {
        return ApiResponse.success(adminAuthService.menus());
    }
}
