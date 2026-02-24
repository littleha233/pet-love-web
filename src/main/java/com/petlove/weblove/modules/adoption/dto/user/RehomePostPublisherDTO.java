package com.petlove.weblove.modules.adoption.dto.user;

public record RehomePostPublisherDTO(
    long userId,
    String nickname,
    String avatarUrl,
    Boolean isRealNameVerified
) {
}
