package com.petlove.weblove.modules.admin.dto;

public record AdminAuthResponse(
    String accessToken,
    long accessTokenExpiresIn,
    String refreshToken,
    long refreshTokenExpiresIn,
    AdminUserDTO admin
) {
}
