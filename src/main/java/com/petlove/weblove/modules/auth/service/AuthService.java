package com.petlove.weblove.modules.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.HashUtil;
import com.petlove.weblove.common.util.MaskUtil;
import com.petlove.weblove.common.util.RandomUtil;
import com.petlove.weblove.config.AppAuthProperties;
import com.petlove.weblove.modules.auth.dto.*;
import com.petlove.weblove.modules.auth.entity.AuthOtpCode;
import com.petlove.weblove.modules.auth.entity.AuthRefreshToken;
import com.petlove.weblove.modules.auth.entity.SmsCodeRecord;
import com.petlove.weblove.modules.auth.entity.SmsSendLog;
import com.petlove.weblove.modules.auth.enums.*;
import com.petlove.weblove.modules.auth.repository.AuthOtpCodeRepository;
import com.petlove.weblove.modules.auth.repository.AuthRefreshTokenRepository;
import com.petlove.weblove.modules.auth.repository.SmsCodeRecordRepository;
import com.petlove.weblove.modules.auth.repository.SmsSendLogRepository;
import com.petlove.weblove.modules.risk.RiskActionKeys;
import com.petlove.weblove.modules.risk.RiskCheckResult;
import com.petlove.weblove.modules.risk.RiskGuard;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final Pattern CN_MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final String REGISTER_CHANNEL_MOBILE_SMS = "MOBILE_SMS";
    private static final String REGISTER_CHANNEL_MOBILE_OTP = "MOBILE_OTP";

    private final AuthOtpCodeRepository otpCodeRepository;
    private final AuthRefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final SmsCodeRecordRepository smsCodeRecordRepository;
    private final SmsSendLogRepository smsSendLogRepository;
    private final SmsProviderClient smsProviderClient;
    private final RiskGuard riskGuard;
    private final JwtService jwtService;
    private final AppAuthProperties authProperties;
    private final ObjectMapper objectMapper;

    public AuthService(AuthOtpCodeRepository otpCodeRepository,
                       AuthRefreshTokenRepository refreshTokenRepository,
                       UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       SmsCodeRecordRepository smsCodeRecordRepository,
                       SmsSendLogRepository smsSendLogRepository,
                       SmsProviderClient smsProviderClient,
                       RiskGuard riskGuard,
                       JwtService jwtService,
                       AppAuthProperties authProperties,
                       ObjectMapper objectMapper) {
        this.otpCodeRepository = otpCodeRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.smsCodeRecordRepository = smsCodeRecordRepository;
        this.smsSendLogRepository = smsSendLogRepository;
        this.smsProviderClient = smsProviderClient;
        this.riskGuard = riskGuard;
        this.jwtService = jwtService;
        this.authProperties = authProperties;
        this.objectMapper = objectMapper;
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

    @Transactional(noRollbackFor = BizException.class)
    public SendSmsCodeResponse sendSmsCode(SendSmsCodeRequest request, String requestIp) {
        String mobile = normalizeChinaMobile(request.getMobile());
        SmsBizType bizType = parseSmsBizType(request.getBizType());
        if (bizType != SmsBizType.LOGIN) {
            throw new BizException(ErrorCode.AUTH_SMS_BIZ_TYPE_INVALID, "Only LOGIN sms bizType is supported");
        }

        AppAuthProperties.SmsProperties sms = authProperties.getSms();
        LocalDateTime now = LocalDateTime.now();
        String normalizedIp = normalizeText(requestIp);

        enforceSmsSendRateLimit(mobile, bizType, normalizedIp, now);
        ensureSmsRiskAllowed(mobile, RiskActionKeys.AUTH_SMS_SEND, ErrorCode.AUTH_SMS_SEND_BLOCKED);

        String code = buildSmsCode(sms);
        SmsCodeRecord codeRecord = new SmsCodeRecord();
        codeRecord.setMobile(mobile);
        codeRecord.setBizType(bizType);
        codeRecord.setCodeHash(hashSmsCode(mobile, bizType, code));
        codeRecord.setStatus(SmsCodeStatus.SENT);
        codeRecord.setExpireAt(now.plusSeconds(sms.getCodeTtlSeconds()));
        codeRecord.setAttemptCount(0);
        codeRecord.setMaxAttempts(Math.max(sms.getMaxAttempts(), 1));
        codeRecord.setClientIp(normalizedIp);
        codeRecord.setDeviceId(normalizeText(request.getDeviceId()));

        SmsProviderSendResult providerResult = smsProviderClient.sendCode(mobile, bizType, code);
        codeRecord.setProvider(normalizeText(providerResult.provider()));
        codeRecord.setProviderTemplateCode(normalizeText(providerResult.templateCode()));
        codeRecord.setProviderMessageId(normalizeText(providerResult.providerMessageId()));
        if (!providerResult.success()) {
            codeRecord.setStatus(SmsCodeStatus.FAILED);
        }
        smsCodeRecordRepository.save(codeRecord);

        SmsSendLog sendLog = new SmsSendLog();
        sendLog.setMobile(mobile);
        sendLog.setBizType(bizType);
        sendLog.setProvider(normalizeText(providerResult.provider(), "unknown"));
        sendLog.setTemplateCode(normalizeText(providerResult.templateCode()));
        sendLog.setSignName(normalizeText(providerResult.signName()));
        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("mobileMasked", MaskUtil.maskMobile(mobile));
        requestPayload.put("bizType", bizType.name());
        requestPayload.put("clientIp", normalizedIp);
        requestPayload.put("deviceId", normalizeText(request.getDeviceId()));
        sendLog.setRequestPayload(toJson(requestPayload));
        sendLog.setResponsePayload(normalizeText(providerResult.responsePayload()));
        sendLog.setSendStatus(providerResult.success() ? SmsSendStatus.SUCCESS : SmsSendStatus.FAILED);
        sendLog.setErrorCode(normalizeText(providerResult.errorCode()));
        sendLog.setErrorMessage(normalizeText(providerResult.errorMessage()));
        sendLog.setProviderMessageId(normalizeText(providerResult.providerMessageId()));
        sendLog.setClientIp(normalizedIp);
        sendLog.setDeviceId(normalizeText(request.getDeviceId()));

        try {
            smsSendLogRepository.save(sendLog);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.AUTH_SMS_LOG_WRITE_FAILED, "SMS send log write failed");
        }

        if (!providerResult.success()) {
            String message = normalizeText(providerResult.errorMessage(), "SMS provider send failed");
            throw new BizException(ErrorCode.AUTH_SMS_PROVIDER_SEND_FAILED, message);
        }

        String mockCode = null;
        if (sms.isExposeMockCodeInResponse() && "mock".equalsIgnoreCase(normalizeText(providerResult.provider(), "mock"))) {
            mockCode = code;
        }

        return new SendSmsCodeResponse(
            true,
            (int) Math.max(sms.getCooldownSeconds(), 1),
            MDC.get("requestId"),
            mockCode
        );
    }

    @Transactional(noRollbackFor = BizException.class)
    public MobileCodeLoginResponse mobileCodeLogin(MobileCodeLoginRequest request) {
        String mobile = normalizeChinaMobile(request.getMobile());
        String code = normalizeRequiredText(request.getCode(), "code");
        LocalDateTime now = LocalDateTime.now();

        ensureSmsRiskAllowed(mobile, RiskActionKeys.AUTH_LOGIN_MOBILE, ErrorCode.AUTH_LOGIN_MOBILE_BLOCKED);

        SmsCodeRecord codeRecord = smsCodeRecordRepository.findTopByMobileAndBizTypeOrderByIdDesc(mobile, SmsBizType.LOGIN)
            .orElseThrow(() -> new BizException(ErrorCode.AUTH_SMS_CODE_NOT_FOUND, "SMS code not found"));

        validateSmsCodeRecord(codeRecord, mobile, code, now);

        MobileUserResolveResult resolveResult = resolveMobileUser(mobile, now);
        User user = resolveResult.user();

        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BizException(ErrorCode.USER_DISABLED, "User is disabled");
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BizException(ErrorCode.USER_BANNED, "User is banned");
        }

        user.setLastLoginAt(now);
        user.setMobileVerifiedAt(now);
        if (normalizeText(user.getRegisterChannel()) == null) {
            user.setRegisterChannel(REGISTER_CHANNEL_MOBILE_SMS);
        }
        if (user.getLoginType() == null) {
            user.setLoginType(LoginType.MOBILE_OTP);
        }
        user = userRepository.save(user);

        UserProfile profile = getOrCreateProfile(user.getId());

        AuthTokenResponse tokenResponse;
        try {
            tokenResponse = issueUserTokens(user, profile, request.getDeviceId());
        } catch (Exception ex) {
            throw new BizException(ErrorCode.AUTH_TOKEN_ISSUE_FAILED, "Token issue failed");
        }

        return new MobileCodeLoginResponse(
            tokenResponse.accessToken(),
            tokenResponse.refreshToken(),
            tokenResponse.accessTokenExpiresIn(),
            resolveResult.isNewUser(),
            new MobileLoginUserInfoDTO(
                user.getId(),
                profile.getNickname(),
                profile.getAvatarUrl(),
                MaskUtil.maskMobile(user.getMobile()),
                profile.isRealNameVerified()
            )
        );
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

    private void enforceSmsSendRateLimit(String mobile, SmsBizType bizType, String clientIp, LocalDateTime now) {
        AppAuthProperties.SmsProperties sms = authProperties.getSms();

        smsSendLogRepository.findTopByMobileAndBizTypeOrderByIdDesc(mobile, bizType)
            .ifPresent(last -> {
                LocalDateTime allowAt = last.getCreatedAt().plusSeconds(Math.max(sms.getCooldownSeconds(), 1));
                if (now.isBefore(allowAt)) {
                    throw new BizException(ErrorCode.AUTH_SMS_SEND_TOO_FREQUENT, "SMS send too frequent");
                }
            });

        LocalDateTime dayStart = now.minusHours(24);
        long mobileCount = smsSendLogRepository.countByMobileAndBizTypeAndCreatedAtGreaterThanEqual(mobile, bizType, dayStart);
        if (mobileCount >= Math.max(sms.getDailyLimitPerMobile(), 1)) {
            throw new BizException(ErrorCode.AUTH_SMS_MOBILE_DAILY_LIMIT_EXCEEDED, "SMS mobile daily limit exceeded");
        }

        if (clientIp != null) {
            long ipCount = smsSendLogRepository.countByClientIpAndCreatedAtGreaterThanEqual(clientIp, dayStart);
            if (ipCount >= Math.max(sms.getDailyLimitPerIp(), 1)) {
                throw new BizException(ErrorCode.AUTH_SMS_IP_DAILY_LIMIT_EXCEEDED, "SMS IP daily limit exceeded");
            }
        }
    }

    private void ensureSmsRiskAllowed(String mobile, String actionKey, ErrorCode blockedCode) {
        Long userId = userRepository.findByMobile(mobile).map(User::getId).orElse(null);
        RiskCheckResult checkResult = riskGuard.checkUserAction(userId, mobile, null, actionKey, null);
        if (!checkResult.allowed()) {
            String message = normalizeText(checkResult.noticeText(), "Current action is blocked by risk rules");
            throw new BizException(blockedCode, message);
        }
    }

    private void validateSmsCodeRecord(SmsCodeRecord codeRecord, String mobile, String inputCode, LocalDateTime now) {
        if (codeRecord.getStatus() == SmsCodeStatus.VERIFIED) {
            throw new BizException(ErrorCode.AUTH_SMS_CODE_ALREADY_USED, "SMS code already used");
        }
        if (codeRecord.getStatus() == SmsCodeStatus.EXPIRED) {
            throw new BizException(ErrorCode.AUTH_SMS_CODE_EXPIRED, "SMS code expired");
        }
        if (codeRecord.getStatus() == SmsCodeStatus.FAILED) {
            if (codeRecord.getAttemptCount() >= codeRecord.getMaxAttempts()) {
                throw new BizException(ErrorCode.AUTH_SMS_CODE_ATTEMPTS_EXCEEDED, "SMS code attempts exceeded");
            }
            throw new BizException(ErrorCode.AUTH_SMS_CODE_STATUS_INVALID, "SMS code status invalid");
        }
        if (codeRecord.getStatus() != SmsCodeStatus.SENT) {
            throw new BizException(ErrorCode.AUTH_SMS_CODE_STATUS_INVALID, "SMS code status invalid");
        }

        if (now.isAfter(codeRecord.getExpireAt())) {
            codeRecord.setStatus(SmsCodeStatus.EXPIRED);
            smsCodeRecordRepository.save(codeRecord);
            throw new BizException(ErrorCode.AUTH_SMS_CODE_EXPIRED, "SMS code expired");
        }

        if (codeRecord.getAttemptCount() >= codeRecord.getMaxAttempts()) {
            codeRecord.setStatus(SmsCodeStatus.FAILED);
            smsCodeRecordRepository.save(codeRecord);
            throw new BizException(ErrorCode.AUTH_SMS_CODE_ATTEMPTS_EXCEEDED, "SMS code attempts exceeded");
        }

        String expectedHash = hashSmsCode(mobile, codeRecord.getBizType(), inputCode);
        if (!Objects.equals(expectedHash, codeRecord.getCodeHash())) {
            int nextAttempts = codeRecord.getAttemptCount() + 1;
            codeRecord.setAttemptCount(nextAttempts);
            if (nextAttempts >= codeRecord.getMaxAttempts()) {
                codeRecord.setStatus(SmsCodeStatus.FAILED);
                smsCodeRecordRepository.save(codeRecord);
                throw new BizException(ErrorCode.AUTH_SMS_CODE_ATTEMPTS_EXCEEDED, "SMS code attempts exceeded");
            }
            smsCodeRecordRepository.save(codeRecord);
            throw new BizException(ErrorCode.AUTH_SMS_CODE_INCORRECT, "SMS code incorrect");
        }

        codeRecord.setStatus(SmsCodeStatus.VERIFIED);
        codeRecord.setVerifiedAt(now);
        smsCodeRecordRepository.save(codeRecord);
    }

    private MobileUserResolveResult resolveMobileUser(String mobile, LocalDateTime now) {
        User existing = userRepository.findByMobile(mobile).orElse(null);
        if (existing != null) {
            return new MobileUserResolveResult(existing, false);
        }

        User user = new User();
        user.setMobile(mobile);
        user.setLoginType(LoginType.MOBILE_OTP);
        user.setStatus(UserStatus.ACTIVE);
        user.setMobileVerifiedAt(now);
        user.setRegisterChannel(REGISTER_CHANNEL_MOBILE_SMS);
        user.setLastLoginAt(now);

        try {
            User saved = userRepository.save(user);
            return new MobileUserResolveResult(saved, true);
        } catch (DataIntegrityViolationException ex) {
            User concurrent = userRepository.findByMobile(mobile).orElseThrow(
                () -> new BizException(ErrorCode.AUTH_USER_CREATE_FAILED, "Create user failed")
            );
            return new MobileUserResolveResult(concurrent, false);
        }
    }

    private User getOrCreateUserForOtp(OtpChannel channel, String target) {
        if (channel == OtpChannel.MOBILE) {
            return userRepository.findByMobile(target).orElseGet(() -> {
                User newUser = new User();
                newUser.setMobile(target);
                newUser.setMobileVerifiedAt(LocalDateTime.now());
                newUser.setRegisterChannel(REGISTER_CHANNEL_MOBILE_OTP);
                newUser.setLoginType(LoginType.MOBILE_OTP);
                newUser.setStatus(UserStatus.ACTIVE);
                return userRepository.save(newUser);
            });
        }

        return userRepository.findByEmail(target).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(target);
            newUser.setLoginType(LoginType.EMAIL_OTP);
            newUser.setStatus(UserStatus.ACTIVE);
            return userRepository.save(newUser);
        });
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

    private SmsBizType parseSmsBizType(String rawBizType) {
        String normalized = normalizeRequiredText(rawBizType, "bizType");
        try {
            return SmsBizType.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.AUTH_SMS_BIZ_TYPE_INVALID, "SMS bizType is invalid");
        }
    }

    private String normalizeChinaMobile(String mobile) {
        String normalized = normalizeRequiredText(mobile, "mobile")
            .replace(" ", "")
            .replace("-", "");
        if (normalized.startsWith("+86")) {
            normalized = normalized.substring(3);
        } else if (normalized.startsWith("86") && normalized.length() > 11) {
            normalized = normalized.substring(2);
        }

        if (!CN_MOBILE_PATTERN.matcher(normalized).matches()) {
            throw new BizException(ErrorCode.AUTH_MOBILE_INVALID, "Mobile format is invalid");
        }
        return normalized;
    }

    private String buildSmsCode(AppAuthProperties.SmsProperties sms) {
        String mockCode = normalizeText(sms.getMockCode());
        if ("mock".equalsIgnoreCase(normalizeText(sms.getProvider(), "mock")) && mockCode != null) {
            return mockCode;
        }

        int length = Math.min(Math.max(sms.getCodeLength(), 4), 8);
        if (length == 6) {
            return RandomUtil.otp6();
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('0' + (int) (Math.random() * 10)));
        }
        return sb.toString();
    }

    private String hashSmsCode(String mobile, SmsBizType bizType, String code) {
        String pepper = normalizeText(authProperties.getSms().getHashPepper(), "petlove-sms-pepper-dev");
        return HashUtil.sha256(pepper + "|" + mobile + "|" + bizType.name() + "|" + code);
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(new LinkedHashMap<>(data));
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw new BizException(ErrorCode.INVALID_PARAM, fieldName + " is required");
        }
        return normalized;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeText(String value, String fallback) {
        String normalized = normalizeText(value);
        return normalized == null ? fallback : normalized;
    }

    private record MobileUserResolveResult(User user, boolean isNewUser) {
    }
}
