package com.petlove.weblove.modules.user.dto;

public record UserProfileDTO(
    long userId,
    String nickname,
    String avatarUrl,
    String cityCode,
    String cityName,
    String bio
) {
}
