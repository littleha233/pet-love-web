package com.petlove.weblove.modules.admin.service;

import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.HashUtil;
import com.petlove.weblove.common.util.RandomUtil;
import com.petlove.weblove.config.AppAuthProperties;
import com.petlove.weblove.modules.admin.dto.*;
import com.petlove.weblove.modules.admin.entity.AdminUser;
import com.petlove.weblove.modules.admin.enums.AdminStatus;
import com.petlove.weblove.modules.admin.repository.AdminUserRepository;
import com.petlove.weblove.modules.auth.dto.LogoutRequest;
import com.petlove.weblove.modules.auth.dto.RefreshTokenRequest;
import com.petlove.weblove.modules.auth.entity.AuthRefreshToken;
import com.petlove.weblove.modules.auth.enums.ClientType;
import com.petlove.weblove.modules.auth.enums.RefreshTokenStatus;
import com.petlove.weblove.modules.auth.repository.AuthRefreshTokenRepository;
import com.petlove.weblove.security.JwtService;
import com.petlove.weblove.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final AuthRefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AppAuthProperties authProperties;

    public AdminAuthService(AdminUserRepository adminUserRepository,
                            AuthRefreshTokenRepository refreshTokenRepository,
                            JwtService jwtService,
                            PasswordEncoder passwordEncoder,
                            AppAuthProperties authProperties) {
        this.adminUserRepository = adminUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
    }

    @Transactional
    public AdminAuthResponse login(AdminLoginRequest request) {
        AdminUser adminUser = adminUserRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED, "Admin username or password is invalid"));

        if (adminUser.getStatus() != AdminStatus.ACTIVE) {
            throw new BizException(ErrorCode.FORBIDDEN, "Admin account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), adminUser.getPasswordHash())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "Admin username or password is invalid");
        }

        adminUser.setLastLoginAt(LocalDateTime.now());
        adminUserRepository.save(adminUser);

        return issueAdminTokens(adminUser, null);
    }

    @Transactional
    public AdminAuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = HashUtil.sha256(request.getRefreshToken());
        AuthRefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new BizException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID, "Refresh token invalid"));

        validateRefreshToken(token);

        AdminUser adminUser = adminUserRepository.findById(token.getAdminUserId())
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Admin user not found"));

        if (adminUser.getStatus() != AdminStatus.ACTIVE) {
            throw new BizException(ErrorCode.FORBIDDEN, "Admin account disabled");
        }

        token.setStatus(RefreshTokenStatus.ROTATED);
        refreshTokenRepository.save(token);

        return issueAdminTokens(adminUser, request.getDeviceId());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        String tokenHash = HashUtil.sha256(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            if (token.getStatus() == RefreshTokenStatus.ACTIVE) {
                token.setStatus(RefreshTokenStatus.REVOKED);
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    @Transactional(readOnly = true)
    public AdminUserDTO me() {
        long adminId = SecurityUtils.currentAdminId();
        AdminUser adminUser = adminUserRepository.findById(adminId)
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Admin user not found"));
        return toDto(adminUser);
    }

    @Transactional(readOnly = true)
    public List<AdminMenuItemDTO> menus() {
        long adminId = SecurityUtils.currentAdminId();
        AdminUser adminUser = adminUserRepository.findById(adminId)
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Admin user not found"));

        List<AdminMenuItemDTO> menus = new ArrayList<>();
        menus.add(new AdminMenuItemDTO("dashboard", "Dashboard", "/dashboard"));
        menus.add(new AdminMenuItemDTO("users", "Users", "/users"));
        menus.add(new AdminMenuItemDTO("verifications", "Verifications", "/verifications"));
        menus.add(new AdminMenuItemDTO("auditLogs", "Audit Logs", "/audit-logs"));

        if (adminUser.getRole().name().equals("SUPER_ADMIN")) {
            menus.add(new AdminMenuItemDTO("settings", "System Settings", "/settings"));
        }
        return menus;
    }

    private AdminAuthResponse issueAdminTokens(AdminUser adminUser, String deviceId) {
        String accessToken = jwtService.createAdminAccessToken(adminUser.getId(), adminUser.getRole().name());
        String refreshTokenRaw = RandomUtil.token();

        AuthRefreshToken refreshToken = new AuthRefreshToken();
        refreshToken.setAdminUserId(adminUser.getId());
        refreshToken.setClientType(ClientType.ADMIN_WEB);
        refreshToken.setDeviceId(deviceId);
        refreshToken.setTokenHash(HashUtil.sha256(refreshTokenRaw));
        refreshToken.setStatus(RefreshTokenStatus.ACTIVE);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(authProperties.getRefreshTokenSeconds()));
        refreshTokenRepository.save(refreshToken);

        return new AdminAuthResponse(
            accessToken,
            authProperties.getAccessTokenSeconds(),
            refreshTokenRaw,
            authProperties.getRefreshTokenSeconds(),
            toDto(adminUser)
        );
    }

    private AdminUserDTO toDto(AdminUser adminUser) {
        return new AdminUserDTO(
            adminUser.getId(),
            adminUser.getUsername(),
            adminUser.getDisplayName(),
            adminUser.getRole().name(),
            adminUser.getStatus().name()
        );
    }

    private void validateRefreshToken(AuthRefreshToken token) {
        if (token.getStatus() != RefreshTokenStatus.ACTIVE) {
            throw new BizException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID, "Refresh token is not active");
        }
        if (token.getClientType() != ClientType.ADMIN_WEB) {
            throw new BizException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID, "Refresh token clientType mismatch");
        }
        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            token.setStatus(RefreshTokenStatus.EXPIRED);
            refreshTokenRepository.save(token);
            throw new BizException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID, "Refresh token expired");
        }
    }
}
