package com.petlove.weblove.modules.auth.service;

import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.HashUtil;
import com.petlove.weblove.common.util.MaskUtil;
import com.petlove.weblove.common.util.RandomUtil;
import com.petlove.weblove.config.AppAuthProperties;
import com.petlove.weblove.modules.auth.dto.*;
import com.petlove.weblove.modules.auth.entity.AuthOtpCode;
import com.petlove.weblove.modules.auth.entity.AuthRefreshToken;
import com.petlove.weblove.modules.auth.enums.*;
import com.petlove.weblove.modules.auth.repository.AuthOtpCodeRepository;
import com.petlove.weblove.modules.auth.repository.AuthRefreshTokenRepository;
import com.petlove.weblove.modules.user.dto.UserProfileDTO;
import com.petlove.weblove.modules.user.entity.User;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.enums.LoginType;
import com.petlove.weblove.modules.user.enums.UserStatus;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.modules.user.repository.UserRepository;
import com.petlove.weblove.security.JwtService;
import com.petlove.weblove.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthOtpCodeRepository otpCodeRepository;
    private final AuthRefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtService jwtService;
    private final AppAuthProperties authProperties;

    public AuthService(AuthOtpCodeRepository otpCodeRepository,
                       AuthRefreshTokenRepository refreshTokenRepository,
                       UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       JwtService jwtService,
                       AppAuthProperties authProperties) {
        this.otpCodeRepository = otpCodeRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtService = jwtService;
        this.authProperties = authProperties;
    }

    @Transactional
    public SendOtpResponse sendOtp(SendOtpRequest request, String requestIp) {
        if (request.getPurpose() != OtpPurpose.LOGIN) {
            throw new BizException(ErrorCode.INVALID_PARAM, "Only LOGIN purpose is supported in phase0");
        }
        String normalizedTarget = request.getTarget().trim();
        String targetHash = HashUtil.sha256(request.getChannel().name() + ":" + normalizedTarget);

        otpCodeRepository.findTopByChannelAndTargetHashAndPurposeOrderByIdDesc(
                request.getChannel(), targetHash, request.getPurpose())
            .ifPresent(last -> {
                LocalDateTime minSendTime = last.getCreatedAt().plusSeconds(authProperties.getOtpResendSeconds());
                if (LocalDateTime.now().isBefore(minSendTime)) {
                    throw new BizException(ErrorCode.AUTH_OTP_TOO_FREQUENT,
                        "OTP request too frequent, please retry later");
                }
            });

        String otpCode = RandomUtil.otp6();
        AuthOtpCode otp = new AuthOtpCode();
        otp.setChannel(request.getChannel());
        otp.setTarget(normalizedTarget);
        otp.setTargetHash(targetHash);
        otp.setPurpose(request.getPurpose());
        otp.setOtpCodeHash(HashUtil.sha256(otpCode));
        otp.setStatus(OtpStatus.ISSUED);
        otp.setExpiresAt(LocalDateTime.now().plusSeconds(authProperties.getOtpTtlSeconds()));
        otp.setRequestIp(requestIp);
        otpCodeRepository.save(otp);

        if (authProperties.isMockOtpEnabled()) {
            log.info("[MOCK_OTP] channel={}, target={}, otp={}", request.getChannel(), normalizedTarget, otpCode);
            return new SendOtpResponse(authProperties.getOtpTtlSeconds(), otpCode);
        }
        return new SendOtpResponse(authProperties.getOtpTtlSeconds(), null);
    }

    @Transactional
    public AuthTokenResponse otpLogin(OtpLoginRequest request) {
        if (request.getClientType() != ClientType.WEB) {
            throw new BizException(ErrorCode.INVALID_PARAM, "clientType must be WEB");
        }
        String normalizedTarget = request.getTarget().trim();
        String targetHash = HashUtil.sha256(request.getChannel().name() + ":" + normalizedTarget);

        AuthOtpCode otpCode = otpCodeRepository
            .findTopByChannelAndTargetHashAndPurposeAndStatusOrderByIdDesc(
                request.getChannel(), targetHash, OtpPurpose.LOGIN, OtpStatus.ISSUED)
            .orElseThrow(() -> new BizException(ErrorCode.AUTH_OTP_INVALID, "OTP is invalid"));

        if (LocalDateTime.now().isAfter(otpCode.getExpiresAt())) {
            otpCode.setStatus(OtpStatus.EXPIRED);
            otpCodeRepository.save(otpCode);
            throw new BizException(ErrorCode.AUTH_OTP_EXPIRED, "OTP is expired");
        }

        if (!Objects.equals(HashUtil.sha256(request.getOtpCode()), otpCode.getOtpCodeHash())) {
            throw new BizException(ErrorCode.AUTH_OTP_INVALID, "OTP is invalid");
        }

        otpCode.setStatus(OtpStatus.USED);
        otpCode.setVerifiedAt(LocalDateTime.now());
        otpCode.setUsedAt(LocalDateTime.now());
        otpCodeRepository.save(otpCode);

        User user = getOrCreateUserForOtp(request.getChannel(), normalizedTarget);
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BizException(ErrorCode.USER_DISABLED, "User is disabled");
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BizException(ErrorCode.USER_BANNED, "User is banned");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        UserProfile profile = getOrCreateProfile(user.getId());
        return issueUserTokens(user, profile, request.getDeviceId());
    }

    @Transactional
    public AuthTokenResponse refreshUserToken(RefreshTokenRequest request) {
        String refreshTokenHash = HashUtil.sha256(request.getRefreshToken());
        AuthRefreshToken token = refreshTokenRepository.findByTokenHash(refreshTokenHash)
            .orElseThrow(() -> new BizException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID, "Refresh token is invalid"));

        validateRefreshToken(token, ClientType.WEB);

        User user = userRepository.findById(token.getUserId())
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "User not found"));

        if (UserStatus.blockedStatuses().contains(user.getStatus())) {
            throw new BizException(ErrorCode.USER_DISABLED, "User status is blocked");
        }

        token.setStatus(RefreshTokenStatus.ROTATED);
        refreshTokenRepository.save(token);

        UserProfile profile = getOrCreateProfile(user.getId());
        return issueUserTokens(user, profile, request.getDeviceId());
    }

    @Transactional
    public void logoutUser(LogoutRequest request) {
        String refreshTokenHash = HashUtil.sha256(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(refreshTokenHash).ifPresent(token -> {
            if (token.getStatus() == RefreshTokenStatus.ACTIVE) {
                token.setStatus(RefreshTokenStatus.REVOKED);
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    @Transactional
    public CurrentUserDTO currentUser() {
        long userId = SecurityUtils.currentUserId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "User not found"));
        UserProfile profile = getOrCreateProfile(user.getId());
        return buildCurrentUserDto(user, profile);
    }

    private User getOrCreateUserForOtp(OtpChannel channel, String target) {
        User user;
        if (channel == OtpChannel.MOBILE) {
            user = userRepository.findByMobile(target).orElseGet(() -> {
                User newUser = new User();
                newUser.setMobile(target);
                newUser.setLoginType(LoginType.MOBILE_OTP);
                newUser.setStatus(UserStatus.ACTIVE);
                return userRepository.save(newUser);
            });
        } else {
            user = userRepository.findByEmail(target).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(target);
                newUser.setLoginType(LoginType.EMAIL_OTP);
                newUser.setStatus(UserStatus.ACTIVE);
                return userRepository.save(newUser);
            });
        }
        return user;
    }

    private UserProfile getOrCreateProfile(Long userId) {
        return userProfileRepository.findByUserId(userId).orElseGet(() -> {
            UserProfile profile = new UserProfile();
            profile.setUserId(userId);
            profile.setNickname("用户" + userId);
            return userProfileRepository.save(profile);
        });
    }

    private AuthTokenResponse issueUserTokens(User user, UserProfile profile, String deviceId) {
        String accessToken = jwtService.createUserAccessToken(user.getId());
        String refreshTokenRaw = RandomUtil.token();

        AuthRefreshToken refreshToken = new AuthRefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setClientType(ClientType.WEB);
        refreshToken.setDeviceId(deviceId);
        refreshToken.setTokenHash(HashUtil.sha256(refreshTokenRaw));
        refreshToken.setStatus(RefreshTokenStatus.ACTIVE);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(authProperties.getRefreshTokenSeconds()));
        refreshTokenRepository.save(refreshToken);

        return new AuthTokenResponse(
            accessToken,
            authProperties.getAccessTokenSeconds(),
            refreshTokenRaw,
            authProperties.getRefreshTokenSeconds(),
            buildCurrentUserDto(user, profile)
        );
    }

    private CurrentUserDTO buildCurrentUserDto(User user, UserProfile profile) {
        List<String> roles = new java.util.ArrayList<>();
        roles.add("USER");
        if (profile.isProviderVerified()) {
            roles.add("PROVIDER");
        }

        return new CurrentUserDTO(
            user.getId(),
            MaskUtil.maskMobile(user.getMobile()),
            MaskUtil.maskEmail(user.getEmail()),
            user.getStatus().name(),
            roles,
            new UserProfileDTO(
                profile.getUserId(),
                profile.getNickname(),
                profile.getAvatarUrl(),
                profile.getCityCode(),
                profile.getCityName(),
                profile.getBio(),
                profile.isRealNameVerified(),
                profile.isProviderVerified()
            )
        );
    }

    private void validateRefreshToken(AuthRefreshToken token, ClientType expectedType) {
        if (token.getStatus() != RefreshTokenStatus.ACTIVE) {
            throw new BizException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID, "Refresh token is not active");
        }
        if (token.getClientType() != expectedType) {
            throw new BizException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID, "Refresh token clientType mismatch");
        }
        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            token.setStatus(RefreshTokenStatus.EXPIRED);
            refreshTokenRepository.save(token);
            throw new BizException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID, "Refresh token expired");
        }
    }
}
