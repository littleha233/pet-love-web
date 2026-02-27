package com.petlove.weblove.modules.auth.dto;

public record MobileCodeLoginResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    boolean isNewUser,
    MobileLoginUserInfoDTO userInfo
) {
}
