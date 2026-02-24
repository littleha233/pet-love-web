package com.petlove.weblove.modules.auth.dto;

import com.petlove.weblove.modules.auth.enums.OtpChannel;
import com.petlove.weblove.modules.auth.enums.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SendOtpRequest {

    @NotNull
    private OtpChannel channel;

    @NotBlank
    private String target;

    @NotNull
    private OtpPurpose purpose;

    private String captchaToken;

    public OtpChannel getChannel() {
        return channel;
    }

    public void setChannel(OtpChannel channel) {
        this.channel = channel;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public OtpPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(OtpPurpose purpose) {
        this.purpose = purpose;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }
}
