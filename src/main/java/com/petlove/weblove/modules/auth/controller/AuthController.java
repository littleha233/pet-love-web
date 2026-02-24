package com.petlove.weblove.modules.auth.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.modules.auth.dto.*;
import com.petlove.weblove.modules.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/otp/send")
    public ApiResponse<SendOtpResponse> sendOtp(@Valid @RequestBody SendOtpRequest request, HttpServletRequest httpServletRequest) {
        return ApiResponse.success(authService.sendOtp(request, httpServletRequest.getRemoteAddr()));
    }

    @PostMapping("/login/otp")
    public ApiResponse<AuthTokenResponse> otpLogin(@Valid @RequestBody OtpLoginRequest request) {
        return ApiResponse.success(authService.otpLogin(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refreshUserToken(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logoutUser(request);
        return ApiResponse.success();
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserDTO> me() {
        return ApiResponse.success(authService.currentUser());
    }
}
