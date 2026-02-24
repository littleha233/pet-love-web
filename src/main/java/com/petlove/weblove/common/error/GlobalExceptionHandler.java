package com.petlove.weblove.common.error;

import com.petlove.weblove.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBizException(BizException ex) {
        HttpStatus status = mapHttpStatus(ex.getErrorCode());
        return ResponseEntity.status(status)
            .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
            .findFirst()
            .map(error -> {
                if (error instanceof FieldError fieldError) {
                    return fieldError.getField() + " " + fieldError.getDefaultMessage();
                }
                return error.getDefaultMessage();
            })
            .orElse("Invalid request");
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INVALID_PARAM, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ErrorCode.INVALID_PARAM, ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ErrorCode.FORBIDDEN, "Access denied"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, ex.getMessage()));
    }

    private HttpStatus mapHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_PARAM -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED,
                 AUTH_OTP_INVALID,
                 AUTH_OTP_EXPIRED,
                 AUTH_TOKEN_INVALID,
                 AUTH_REFRESH_TOKEN_INVALID -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN,
                 USER_DISABLED,
                 USER_BANNED,
                 AUTH_OTP_TOO_FREQUENT,
                 STATE_TRANSITION_INVALID -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
