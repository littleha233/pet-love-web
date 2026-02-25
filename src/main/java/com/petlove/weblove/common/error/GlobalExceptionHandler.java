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
            case INVALID_PARAM,
                 VERIFICATION_STATUS_INVALID,
                 VERIFICATION_FILE_INVALID,
                 VERIFICATION_ALREADY_APPROVED,
                 VERIFICATION_REAL_NAME_REQUIRED,
                 ADOPTION_REAL_NAME_REQUIRED,
                 ADOPTION_POST_STATUS_INVALID,
                 ADOPTION_POST_FILE_INVALID,
                 ADOPTION_APPLICATION_DUPLICATE,
                 FEEDING_REAL_NAME_REQUIRED,
                 FEEDING_PROVIDER_PROFILE_STATUS_INVALID,
                 FEEDING_ORDER_STATUS_INVALID,
                 FEEDING_ORDER_CANNOT_CANCEL,
                 FEEDING_ORDER_CANNOT_CONFIRM,
                 FEEDING_ORDER_PET_INVALID,
                 FEEDING_REVIEW_ALREADY_EXISTS,
                 FEEDING_REVIEW_NOT_ALLOWED,
                 FEEDING_VISIT_STATUS_INVALID,
                 FEEDING_VISIT_FILE_INVALID,
                 RESCUE_GUIDE_STATUS_INVALID,
                 RESCUE_RESOURCE_STATUS_INVALID,
                 RESCUE_CLUE_STATUS_INVALID,
                 RESCUE_CLUE_FILE_INVALID,
                 RESCUE_CLUE_SUGGESTED_RESOURCE_INVALID,
                 COMPLAINT_TICKET_STATUS_INVALID,
                 COMPLAINT_TICKET_FILE_INVALID,
                 RISK_CONFIG_INVALID -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED,
                 AUTH_OTP_INVALID,
                 AUTH_OTP_EXPIRED,
                 AUTH_TOKEN_INVALID,
                 AUTH_REFRESH_TOKEN_INVALID -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN,
                 USER_DISABLED,
                 USER_BANNED,
                 AUTH_OTP_TOO_FREQUENT,
                 STATE_TRANSITION_INVALID,
                 VERIFICATION_FILE_NOT_OWNED,
                 VERIFICATION_SUBMIT_NOT_ALLOWED,
                 VERIFICATION_REVIEW_NOT_ALLOWED,
                 ADOPTION_POST_NOT_OWNER,
                 ADOPTION_POST_REVIEW_NOT_ALLOWED,
                 ADOPTION_POST_FILE_NOT_OWNED,
                 ADOPTION_APPLICATION_NOT_ALLOWED,
                 ADOPTION_APPLICATION_NOT_OWNER,
                 ADOPTION_APPLICATION_HANDLE_NOT_ALLOWED,
                 ADOPTION_CANNOT_APPLY_OWN_POST,
                 FEEDING_PROVIDER_VERIFICATION_REQUIRED,
                 FEEDING_ORDER_NOT_OWNER,
                 FEEDING_ORDER_NOT_PROVIDER,
                 FEEDING_ORDER_PET_NOT_OWNED,
                 FEEDING_VISIT_NOT_PROVIDER,
                 FEEDING_VISIT_FILE_NOT_OWNED,
                 RESCUE_CLUE_NOT_OWNER,
                 RESCUE_CLUE_FILE_NOT_OWNED,
                 RESCUE_PERMISSION_DENIED,
                 COMPLAINT_TICKET_NOT_OWNER,
                 COMPLAINT_TICKET_FILE_NOT_OWNED,
                 RISK_BLACKLIST_BLOCKED,
                 RISK_ACTION_NOT_ALLOWED,
                 RISK_SCOPE_NOT_ALLOWED,
                 CITY_FEATURE_NOT_OPEN,
                 CITY_FEATURE_READ_DISABLED,
                 CITY_FEATURE_WRITE_DISABLED -> HttpStatus.FORBIDDEN;
            case NOT_FOUND,
                 VERIFICATION_NOT_FOUND,
                 ADOPTION_POST_NOT_FOUND,
                 ADOPTION_APPLICATION_NOT_FOUND,
                 FEEDING_PROVIDER_PROFILE_NOT_FOUND,
                 FEEDING_ORDER_NOT_FOUND,
                 FEEDING_VISIT_NOT_FOUND,
                 RESCUE_GUIDE_NOT_FOUND,
                 RESCUE_RESOURCE_NOT_FOUND,
                 RESCUE_CLUE_NOT_FOUND,
                 COMPLAINT_TICKET_NOT_FOUND,
                 AUDIT_LOG_NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
