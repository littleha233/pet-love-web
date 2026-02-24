package com.petlove.weblove.modules.auth.dto;

import com.petlove.weblove.modules.auth.enums.ClientType;
import com.petlove.weblove.modules.auth.enums.OtpChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OtpLoginRequest {

    @NotNull
    private OtpChannel channel;

    @NotBlank
    private String target;

    @NotBlank
    private String otpCode;

    private String deviceId;

    @NotNull
    private ClientType clientType;

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

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }
}
