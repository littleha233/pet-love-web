package com.petlove.weblove.modules.risk;

import com.petlove.weblove.common.error.ErrorCode;

public record RiskCheckResult(
    boolean allowed,
    ErrorCode errorCode,
    String noticeText
) {

    public static RiskCheckResult allow() {
        return new RiskCheckResult(true, null, null);
    }

    public static RiskCheckResult deny(ErrorCode errorCode, String noticeText) {
        return new RiskCheckResult(false, errorCode, noticeText);
    }
}
