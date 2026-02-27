package com.petlove.weblove.modules.auth.service;

import com.petlove.weblove.common.util.RandomUtil;
import com.petlove.weblove.config.AppAuthProperties;
import com.petlove.weblove.modules.auth.enums.SmsBizType;
import org.springframework.stereotype.Service;

@Service
public class MockSmsProviderClient implements SmsProviderClient {

    private final AppAuthProperties authProperties;

    public MockSmsProviderClient(AppAuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public SmsProviderSendResult sendCode(String mobile, SmsBizType bizType, String code) {
        AppAuthProperties.SmsProperties sms = authProperties.getSms();
        String provider = normalize(sms.getProvider(), "mock");
        String templateCode = normalize(sms.getTemplateLoginCode(), "LOGIN_CODE");
        String signName = normalize(sms.getSignName(), "PetLove");

        if (!sms.isEnabled()) {
            return SmsProviderSendResult.failed(
                provider,
                templateCode,
                signName,
                "SMS_DISABLED",
                "SMS service is disabled",
                "{\"accepted\":false,\"reason\":\"SMS_DISABLED\"}"
            );
        }

        if (!"mock".equalsIgnoreCase(provider)) {
            return SmsProviderSendResult.failed(
                provider,
                templateCode,
                signName,
                "SMS_PROVIDER_NOT_IMPLEMENTED",
                "Configured provider is not implemented",
                "{\"accepted\":false,\"reason\":\"SMS_PROVIDER_NOT_IMPLEMENTED\"}"
            );
        }

        String providerMessageId = "mock_" + RandomUtil.token().substring(0, 16);
        return SmsProviderSendResult.success(
            provider,
            templateCode,
            signName,
            providerMessageId,
            "{\"accepted\":true,\"provider\":\"mock\"}"
        );
    }

    private String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
