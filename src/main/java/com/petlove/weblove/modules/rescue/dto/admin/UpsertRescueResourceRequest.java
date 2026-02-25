package com.petlove.weblove.modules.rescue.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UpsertRescueResourceRequest {

    @NotBlank
    @Size(max = 32)
    private String resourceType;

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @Size(max = 32)
    private String cityCode;

    @NotBlank
    @Size(max = 64)
    private String cityName;

    @Size(max = 64)
    private String districtName;

    @Size(max = 255)
    private String address;

    @Size(max = 64)
    private String contactPhone;

    @Size(max = 64)
    private String contactWechat;

    @Size(max = 255)
    private String contactOther;

    @Size(max = 128)
    private String serviceHours;

    @Size(max = 255)
    private String serviceScope;

    @Size(max = 10)
    private List<@NotBlank @Size(max = 16) String> acceptPetTypes;

    @Size(max = 20)
    private List<@NotBlank @Size(max = 32) String> capabilityTags;

    private String description;

    @Size(max = 500)
    private String sourceUrl;

    @Size(max = 32)
    private String verifiedAt;

    private Integer sortOrder;

    @Size(max = 32)
    private String status;

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactWechat() {
        return contactWechat;
    }

    public void setContactWechat(String contactWechat) {
        this.contactWechat = contactWechat;
    }

    public String getContactOther() {
        return contactOther;
    }

    public void setContactOther(String contactOther) {
        this.contactOther = contactOther;
    }

    public String getServiceHours() {
        return serviceHours;
    }

    public void setServiceHours(String serviceHours) {
        this.serviceHours = serviceHours;
    }

    public String getServiceScope() {
        return serviceScope;
    }

    public void setServiceScope(String serviceScope) {
        this.serviceScope = serviceScope;
    }

    public List<String> getAcceptPetTypes() {
        return acceptPetTypes;
    }

    public void setAcceptPetTypes(List<String> acceptPetTypes) {
        this.acceptPetTypes = acceptPetTypes;
    }

    public List<String> getCapabilityTags() {
        return capabilityTags;
    }

    public void setCapabilityTags(List<String> capabilityTags) {
        this.capabilityTags = capabilityTags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(String verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
