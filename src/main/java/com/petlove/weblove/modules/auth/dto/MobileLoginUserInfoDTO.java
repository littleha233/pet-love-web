package com.petlove.weblove.modules.auth.dto;

public record MobileLoginUserInfoDTO(
    long userId,
    String nickname,
    String avatarUrl,
    String mobileMasked,
    boolean realNameVerified
) {
}
