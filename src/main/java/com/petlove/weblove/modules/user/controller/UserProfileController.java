package com.petlove.weblove.modules.user.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.modules.user.dto.UpdateUserProfileRequest;
import com.petlove.weblove.modules.user.dto.UserProfileDTO;
import com.petlove.weblove.modules.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ApiResponse<UserProfileDTO> getMyProfile() {
        return ApiResponse.success(userProfileService.getMyProfile());
    }

    @PatchMapping
    public ApiResponse<UserProfileDTO> updateMyProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        return ApiResponse.success(userProfileService.updateMyProfile(request));
    }
}
