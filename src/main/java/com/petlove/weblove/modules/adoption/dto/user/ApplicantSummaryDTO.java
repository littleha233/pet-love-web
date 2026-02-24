package com.petlove.weblove.modules.adoption.dto.user;

public record ApplicantSummaryDTO(
    long userId,
    String nickname,
    String avatarUrl,
    Boolean isRealNameVerified
) {
}
