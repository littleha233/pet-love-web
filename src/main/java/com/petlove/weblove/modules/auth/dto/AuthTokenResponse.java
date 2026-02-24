package com.petlove.weblove.modules.auth.dto;

public record AuthTokenResponse(
    String accessToken,
    long accessTokenExpiresIn,
    String refreshToken,
    long refreshTokenExpiresIn,
    CurrentUserDTO user
) {
}
