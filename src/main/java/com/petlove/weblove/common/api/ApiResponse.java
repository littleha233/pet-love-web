package com.petlove.weblove.common.api;

import com.petlove.weblove.common.error.ErrorCode;
import org.slf4j.MDC;

public record ApiResponse<T>(
    String code,
    String message,
    String requestId,
    T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCode.OK.name(), "success", currentRequestId(), data);
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.name(), message, currentRequestId(), null);
    }

    private static String currentRequestId() {
        return MDC.get("requestId");
    }
}
