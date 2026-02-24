package com.petlove.weblove.modules.admin.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.admin.dto.AdminUserListItemDTO;
import com.petlove.weblove.modules.admin.dto.UpdateUserStatusRequest;
import com.petlove.weblove.modules.admin.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminUserListItemDTO>> listUsers(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(adminUserService.listUsers(page, pageSize, status, keyword));
    }

    @PatchMapping("/{userId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long userId,
                                          @Valid @RequestBody UpdateUserStatusRequest request) {
        adminUserService.updateUserStatus(userId, request);
        return ApiResponse.success();
    }
}
