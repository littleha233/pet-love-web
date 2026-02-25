package com.petlove.weblove.modules.rescue.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.rescue.enums.RescueResourceStatus;
import com.petlove.weblove.modules.rescue.enums.RescueResourceType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_resources")
public class RescueResource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private RescueResourceType resourceType;

    @Column(nullable = false)
    private String name;

    @Column(name = "city_code", nullable = false)
    private String cityCode;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    @Column(name = "district_name")
    private String districtName;

    private String address;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_wechat")
    private String contactWechat;

    @Column(name = "contact_other")
    private String contactOther;

    @Column(name = "service_hours")
    private String serviceHours;

    @Column(name = "service_scope")
    private String serviceScope;

    @Column(name = "accept_pet_types", columnDefinition = "JSON")
    private String acceptPetTypes;

    @Column(name = "capability_tags", columnDefinition = "JSON")
    private String capabilityTags;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RescueResourceStatus status;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_by_admin_id")
    private Long createdByAdminId;

    @Column(name = "updated_by_admin_id")
    private Long updatedByAdminId;

    public Long getId() {
        return id;
    }

    public RescueResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(RescueResourceType resourceType) {
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

    public String getAcceptPetTypes() {
        return acceptPetTypes;
    }

    public void setAcceptPetTypes(String acceptPetTypes) {
        this.acceptPetTypes = acceptPetTypes;
    }

    public String getCapabilityTags() {
        return capabilityTags;
    }

    public void setCapabilityTags(String capabilityTags) {
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

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public RescueResourceStatus getStatus() {
        return status;
    }

    public void setStatus(RescueResourceStatus status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getCreatedByAdminId() {
        return createdByAdminId;
    }

    public void setCreatedByAdminId(Long createdByAdminId) {
        this.createdByAdminId = createdByAdminId;
    }

    public Long getUpdatedByAdminId() {
        return updatedByAdminId;
    }

    public void setUpdatedByAdminId(Long updatedByAdminId) {
        this.updatedByAdminId = updatedByAdminId;
    }
}
