package com.petlove.weblove.modules.rescue.dto.user;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class SubmitRescueClueRequest {

    @NotBlank
    @Size(max = 32)
    private String cityCode;

    @NotBlank
    @Size(max = 64)
    private String cityName;

    @Size(max = 64)
    private String districtName;

    @NotBlank
    @Size(max = 255)
    private String locationText;

    @DecimalMin("-90.0000000")
    @DecimalMax("90.0000000")
    private BigDecimal geoLat;

    @DecimalMin("-180.0000000")
    @DecimalMax("180.0000000")
    private BigDecimal geoLng;

    @Size(max = 16)
    private String petType;

    @Min(1)
    @Max(50)
    private Integer estimatedCount;

    @NotBlank
    @Size(max = 16)
    private String urgencyLevel;

    @Size(max = 10)
    private List<@NotBlank @Size(max = 32) String> conditionTags;

    @NotBlank
    @Size(max = 5000)
    private String description;

    @NotBlank
    @Size(max = 64)
    private String contactName;

    @NotBlank
    @Size(max = 32)
    private String contactMobile;

    @Size(max = 9)
    private List<@NotNull Long> photoFileIds;

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

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public BigDecimal getGeoLat() {
        return geoLat;
    }

    public void setGeoLat(BigDecimal geoLat) {
        this.geoLat = geoLat;
    }

    public BigDecimal getGeoLng() {
        return geoLng;
    }

    public void setGeoLng(BigDecimal geoLng) {
        this.geoLng = geoLng;
    }

    public String getPetType() {
        return petType;
    }

    public void setPetType(String petType) {
        this.petType = petType;
    }

    public Integer getEstimatedCount() {
        return estimatedCount;
    }

    public void setEstimatedCount(Integer estimatedCount) {
        this.estimatedCount = estimatedCount;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public List<String> getConditionTags() {
        return conditionTags;
    }

    public void setConditionTags(List<String> conditionTags) {
        this.conditionTags = conditionTags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactMobile() {
        return contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }

    public List<Long> getPhotoFileIds() {
        return photoFileIds;
    }

    public void setPhotoFileIds(List<Long> photoFileIds) {
        this.photoFileIds = photoFileIds;
    }
}
