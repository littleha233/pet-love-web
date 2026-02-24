package com.petlove.weblove.modules.auth.dto;

public record SendOtpResponse(
    long ttlSeconds,
    String mockCode
) {
}
