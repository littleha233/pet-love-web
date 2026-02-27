package com.petlove.weblove.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AppAuthProperties {

    private String jwtSecret;
    private long accessTokenSeconds;
    private long refreshTokenSeconds;
    private long otpTtlSeconds;
    private long otpResendSeconds;
    private boolean mockOtpEnabled;
    private SmsProperties sms = new SmsProperties();

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getAccessTokenSeconds() {
        return accessTokenSeconds;
    }

    public void setAccessTokenSeconds(long accessTokenSeconds) {
        this.accessTokenSeconds = accessTokenSeconds;
    }

    public long getRefreshTokenSeconds() {
        return refreshTokenSeconds;
    }

    public void setRefreshTokenSeconds(long refreshTokenSeconds) {
        this.refreshTokenSeconds = refreshTokenSeconds;
    }

    public long getOtpTtlSeconds() {
        return otpTtlSeconds;
    }

    public void setOtpTtlSeconds(long otpTtlSeconds) {
        this.otpTtlSeconds = otpTtlSeconds;
    }

    public long getOtpResendSeconds() {
        return otpResendSeconds;
    }

    public void setOtpResendSeconds(long otpResendSeconds) {
        this.otpResendSeconds = otpResendSeconds;
    }

    public boolean isMockOtpEnabled() {
        return mockOtpEnabled;
    }

    public void setMockOtpEnabled(boolean mockOtpEnabled) {
        this.mockOtpEnabled = mockOtpEnabled;
    }

    public SmsProperties getSms() {
        return sms;
    }

    public void setSms(SmsProperties sms) {
        this.sms = sms;
    }

    public static class SmsProperties {

        private boolean enabled = true;
        private String provider = "mock";
        private String signName = "PetLove";
        private String templateLoginCode = "LOGIN_CODE";
        private long codeTtlSeconds = 300;
        private int codeLength = 6;
        private long cooldownSeconds = 60;
        private int dailyLimitPerMobile = 10;
        private int dailyLimitPerIp = 30;
        private int maxAttempts = 5;
        private String hashPepper = "petlove-sms-pepper-dev";
        private boolean exposeMockCodeInResponse = true;
        private String mockCode = "123456";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getSignName() {
            return signName;
        }

        public void setSignName(String signName) {
            this.signName = signName;
        }

        public String getTemplateLoginCode() {
            return templateLoginCode;
        }

        public void setTemplateLoginCode(String templateLoginCode) {
            this.templateLoginCode = templateLoginCode;
        }

        public long getCodeTtlSeconds() {
            return codeTtlSeconds;
        }

        public void setCodeTtlSeconds(long codeTtlSeconds) {
            this.codeTtlSeconds = codeTtlSeconds;
        }

        public int getCodeLength() {
            return codeLength;
        }

        public void setCodeLength(int codeLength) {
            this.codeLength = codeLength;
        }

        public long getCooldownSeconds() {
            return cooldownSeconds;
        }

        public void setCooldownSeconds(long cooldownSeconds) {
            this.cooldownSeconds = cooldownSeconds;
        }

        public int getDailyLimitPerMobile() {
            return dailyLimitPerMobile;
        }

        public void setDailyLimitPerMobile(int dailyLimitPerMobile) {
            this.dailyLimitPerMobile = dailyLimitPerMobile;
        }

        public int getDailyLimitPerIp() {
            return dailyLimitPerIp;
        }

        public void setDailyLimitPerIp(int dailyLimitPerIp) {
            this.dailyLimitPerIp = dailyLimitPerIp;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public String getHashPepper() {
            return hashPepper;
        }

        public void setHashPepper(String hashPepper) {
            this.hashPepper = hashPepper;
        }

        public boolean isExposeMockCodeInResponse() {
            return exposeMockCodeInResponse;
        }

        public void setExposeMockCodeInResponse(boolean exposeMockCodeInResponse) {
            this.exposeMockCodeInResponse = exposeMockCodeInResponse;
        }

        public String getMockCode() {
            return mockCode;
        }

        public void setMockCode(String mockCode) {
            this.mockCode = mockCode;
        }
    }
}
