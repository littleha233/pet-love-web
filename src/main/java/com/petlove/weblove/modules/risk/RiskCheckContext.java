package com.petlove.weblove.modules.risk;

public record RiskCheckContext(
    Long userId,
    String mobile,
    String cityCode,
    String actionKey
) {
}
