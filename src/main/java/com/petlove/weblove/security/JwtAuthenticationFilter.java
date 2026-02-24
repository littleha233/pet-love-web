package com.petlove.weblove.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.admin.entity.AdminUser;
import com.petlove.weblove.modules.admin.enums.AdminStatus;
import com.petlove.weblove.modules.admin.repository.AdminUserRepository;
import com.petlove.weblove.modules.user.entity.User;
import com.petlove.weblove.modules.user.enums.UserStatus;
import com.petlove.weblove.modules.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserRepository userRepository,
                                   AdminUserRepository adminUserRepository,
                                   ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        ParsedToken parsedToken;
        try {
            parsedToken = jwtService.parseAccessToken(token);
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.AUTH_TOKEN_INVALID, "Token is invalid or expired");
            return;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/") && parsedToken.tokenType() != JwtTokenType.ADMIN_ACCESS) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.AUTH_TOKEN_INVALID, "Admin token required");
            return;
        }

        if (path.startsWith("/api/v1/") && isUserProtectedPath(path) && parsedToken.tokenType() != JwtTokenType.USER_ACCESS) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.AUTH_TOKEN_INVALID, "User token required");
            return;
        }

        if (parsedToken.tokenType() == JwtTokenType.USER_ACCESS) {
            User user = userRepository.findById(parsedToken.subjectId()).orElse(null);
            if (user == null) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.AUTH_TOKEN_INVALID, "User not found");
                return;
            }
            if (user.getStatus() == UserStatus.DISABLED) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.USER_DISABLED, "User is disabled");
                return;
            }
            if (user.getStatus() == UserStatus.BANNED) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.USER_BANNED, "User is banned");
                return;
            }

            AuthPrincipal principal = new AuthPrincipal(user.getId(), AuthPrincipalType.USER, "USER");
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        if (parsedToken.tokenType() == JwtTokenType.ADMIN_ACCESS) {
            AdminUser adminUser = adminUserRepository.findById(parsedToken.subjectId()).orElse(null);
            if (adminUser == null) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.AUTH_TOKEN_INVALID, "Admin not found");
                return;
            }
            if (adminUser.getStatus() != AdminStatus.ACTIVE) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.FORBIDDEN, "Admin account disabled");
                return;
            }

            AuthPrincipal principal = new AuthPrincipal(adminUser.getId(), AuthPrincipalType.ADMIN, adminUser.getRole().name());
            List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_" + adminUser.getRole().name())
            );
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isUserProtectedPath(String path) {
        return !(path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/system/") || path.startsWith("/api/v1/meta/"));
    }

    private void writeError(HttpServletResponse response,
                            int status,
                            ErrorCode errorCode,
                            String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(errorCode, message));
    }
}
