package com.petlove.weblove.security;

import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Unauthorized");
        }
        return principal;
    }

    public static long currentUserId() {
        AuthPrincipal principal = currentPrincipal();
        if (principal.type() != AuthPrincipalType.USER) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "User token required");
        }
        return principal.id();
    }

    public static long currentAdminId() {
        AuthPrincipal principal = currentPrincipal();
        if (principal.type() != AuthPrincipalType.ADMIN) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Admin token required");
        }
        return principal.id();
    }
}
