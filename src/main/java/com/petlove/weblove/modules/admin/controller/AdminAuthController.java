package com.petlove.weblove.modules.admin.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.modules.admin.dto.*;
import com.petlove.weblove.modules.admin.service.AdminAuthService;
import com.petlove.weblove.modules.auth.dto.LogoutRequest;
import com.petlove.weblove.modules.auth.dto.RefreshTokenRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<AdminAuthResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success(adminAuthService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AdminAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(adminAuthService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        adminAuthService.logout(request);
        return ApiResponse.success();
    }

    @GetMapping("/me")
    public ApiResponse<AdminUserDTO> me() {
        return ApiResponse.success(adminAuthService.me());
    }
}
