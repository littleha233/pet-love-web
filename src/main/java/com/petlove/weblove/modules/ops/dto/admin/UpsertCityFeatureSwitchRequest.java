package com.petlove.weblove.modules.ops.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpsertCityFeatureSwitchRequest {

    @NotBlank
    @Size(max = 32)
    private String cityCode;

    @NotBlank
    @Size(max = 64)
    private String cityName;

    @NotBlank
    @Size(max = 64)
    private String featureKey;

    @NotNull
    private Boolean isEnabled;

    @NotNull
    private Boolean allowRead;

    @NotNull
    private Boolean allowWrite;

    @Size(max = 255)
    private String noticeText;

    @Size(max = 32)
    private String effectiveFrom;

    @Size(max = 32)
    private String effectiveTo;

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Boolean getAllowRead() {
        return allowRead;
    }

    public void setAllowRead(Boolean allowRead) {
        this.allowRead = allowRead;
    }

    public Boolean getAllowWrite() {
        return allowWrite;
    }

    public void setAllowWrite(Boolean allowWrite) {
        this.allowWrite = allowWrite;
    }

    public String getNoticeText() {
        return noticeText;
    }

    public void setNoticeText(String noticeText) {
        this.noticeText = noticeText;
    }

    public String getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(String effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public String getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(String effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
}
