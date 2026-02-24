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
}
