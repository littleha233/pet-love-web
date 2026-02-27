package com.petlove.weblove.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class MobileCodeLoginRequest {

    @NotBlank
    private String mobile;

    @NotBlank
    private String code;

    private String deviceId;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
