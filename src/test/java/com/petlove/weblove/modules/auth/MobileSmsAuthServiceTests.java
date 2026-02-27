package com.petlove.weblove.modules.auth;

import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.auth.dto.MobileCodeLoginRequest;
import com.petlove.weblove.modules.auth.dto.MobileCodeLoginResponse;
import com.petlove.weblove.modules.auth.dto.SendSmsCodeRequest;
import com.petlove.weblove.modules.auth.dto.SendSmsCodeResponse;
import com.petlove.weblove.modules.auth.entity.SmsCodeRecord;
import com.petlove.weblove.modules.auth.entity.SmsSendLog;
import com.petlove.weblove.modules.auth.enums.SmsBizType;
import com.petlove.weblove.modules.auth.enums.SmsCodeStatus;
import com.petlove.weblove.modules.auth.repository.SmsCodeRecordRepository;
import com.petlove.weblove.modules.auth.repository.SmsSendLogRepository;
import com.petlove.weblove.modules.auth.service.AuthService;
import com.petlove.weblove.modules.user.entity.User;
import com.petlove.weblove.modules.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MobileSmsAuthServiceTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SmsCodeRecordRepository smsCodeRecordRepository;

    @Autowired
    private SmsSendLogRepository smsSendLogRepository;

    @Test
    void mobileCodeLogin_autoRegistersNewUser() {
        String mobile = "13800138000";

        SendSmsCodeRequest sendRequest = new SendSmsCodeRequest();
        sendRequest.setMobile(mobile);
        sendRequest.setBizType("LOGIN");
        sendRequest.setDeviceId("test-device-1");

        SendSmsCodeResponse sendResponse = authService.sendSmsCode(sendRequest, "127.0.0.1");
        assertTrue(sendResponse.success());
        assertTrue(sendResponse.cooldownSeconds() > 0);

        MobileCodeLoginRequest loginRequest = new MobileCodeLoginRequest();
        loginRequest.setMobile(mobile);
        loginRequest.setCode("123456");
        loginRequest.setDeviceId("test-device-1");

        MobileCodeLoginResponse loginResponse = authService.mobileCodeLogin(loginRequest);
        assertNotNull(loginResponse.accessToken());
        assertNotNull(loginResponse.refreshToken());
        assertTrue(loginResponse.isNewUser());
        assertEquals("用户" + loginResponse.userInfo().userId(), loginResponse.userInfo().nickname());

        User user = userRepository.findByMobile(mobile).orElseThrow();
        assertNotNull(user.getMobileVerifiedAt());
        assertEquals("MOBILE_SMS", user.getRegisterChannel());

        SmsCodeRecord codeRecord = smsCodeRecordRepository.findTopByMobileAndBizTypeOrderByIdDesc(mobile, SmsBizType.LOGIN)
            .orElseThrow();
        assertEquals(SmsCodeStatus.VERIFIED, codeRecord.getStatus());

        List<SmsSendLog> logs = smsSendLogRepository.findAll();
        assertFalse(logs.isEmpty());
    }

    @Test
    void sendSmsCode_tooFrequentBlockedByCooldown() {
        String mobile = "13800138001";

        SendSmsCodeRequest request = new SendSmsCodeRequest();
        request.setMobile(mobile);
        request.setBizType("LOGIN");

        authService.sendSmsCode(request, "127.0.0.1");

        BizException ex = assertThrows(BizException.class, () -> authService.sendSmsCode(request, "127.0.0.1"));
        assertEquals(ErrorCode.AUTH_SMS_SEND_TOO_FREQUENT, ex.getErrorCode());
    }

    @Test
    void mobileCodeLogin_wrongCodeEventuallyExceedsAttempts() {
        String mobile = "13800138002";

        SendSmsCodeRequest sendRequest = new SendSmsCodeRequest();
        sendRequest.setMobile(mobile);
        sendRequest.setBizType("LOGIN");

        authService.sendSmsCode(sendRequest, "127.0.0.1");

        MobileCodeLoginRequest loginRequest = new MobileCodeLoginRequest();
        loginRequest.setMobile(mobile);
        loginRequest.setCode("000000");

        for (int i = 0; i < 4; i++) {
            BizException ex = assertThrows(BizException.class, () -> authService.mobileCodeLogin(loginRequest));
            assertEquals(ErrorCode.AUTH_SMS_CODE_INCORRECT, ex.getErrorCode());
        }

        BizException exceedEx = assertThrows(BizException.class, () -> authService.mobileCodeLogin(loginRequest));
        assertEquals(ErrorCode.AUTH_SMS_CODE_ATTEMPTS_EXCEEDED, exceedEx.getErrorCode());

        SmsCodeRecord codeRecord = smsCodeRecordRepository.findTopByMobileAndBizTypeOrderByIdDesc(mobile, SmsBizType.LOGIN)
            .orElseThrow();
        assertEquals(SmsCodeStatus.FAILED, codeRecord.getStatus());
        assertEquals(5, codeRecord.getAttemptCount());
    }
}
