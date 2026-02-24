package com.petlove.weblove.modules.verification.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.modules.verification.dto.user.MyVerificationOverviewDTO;
import com.petlove.weblove.modules.verification.dto.user.SubmitProviderVerificationRequest;
import com.petlove.weblove.modules.verification.dto.user.SubmitRealNameVerificationRequest;
import com.petlove.weblove.modules.verification.dto.user.VerificationDetailDTO;
import com.petlove.weblove.modules.verification.dto.user.VerificationStatusDTO;
import com.petlove.weblove.modules.verification.enums.VerificationType;
import com.petlove.weblove.modules.verification.service.UserVerificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/verifications")
public class UserVerificationController {

    private final UserVerificationService userVerificationService;

    public UserVerificationController(UserVerificationService userVerificationService) {
        this.userVerificationService = userVerificationService;
    }

    @GetMapping("/me")
    public ApiResponse<MyVerificationOverviewDTO> myOverview() {
        return ApiResponse.success(userVerificationService.getMyOverview());
    }

    @GetMapping("/{type}")
    public ApiResponse<VerificationDetailDTO> myDetail(@PathVariable VerificationType type) {
        return ApiResponse.success(userVerificationService.getMyDetail(type));
    }

    @PostMapping("/real-name/submit")
    public ApiResponse<VerificationStatusDTO> submitRealName(@Valid @RequestBody SubmitRealNameVerificationRequest request) {
        return ApiResponse.success(userVerificationService.submitRealName(request));
    }

    @PostMapping("/provider/submit")
    public ApiResponse<VerificationStatusDTO> submitProvider(@Valid @RequestBody SubmitProviderVerificationRequest request) {
        return ApiResponse.success(userVerificationService.submitProvider(request));
    }
}
