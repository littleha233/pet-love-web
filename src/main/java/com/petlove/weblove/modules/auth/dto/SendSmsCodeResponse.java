package com.petlove.weblove.modules.auth.dto;

public record SendSmsCodeResponse(
    boolean success,
    int cooldownSeconds,
    String traceId,
    String mockCode
) {
}
