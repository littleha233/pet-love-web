package com.petlove.weblove.modules.rescue.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.rescue.enums.RescueClueStatus;
import com.petlove.weblove.modules.rescue.enums.RescuePetType;
import com.petlove.weblove.modules.rescue.enums.RescueUrgencyLevel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_clues")
public class RescueClue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clue_no", nullable = false, unique = true)
    private String clueNo;

    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    @Column(name = "city_code", nullable = false)
    private String cityCode;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "location_text", nullable = false)
    private String locationText;

    @Column(name = "geo_lat")
    private BigDecimal geoLat;

    @Column(name = "geo_lng")
    private BigDecimal geoLng;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_type")
    private RescuePetType petType;

    @Column(name = "estimated_count")
    private Integer estimatedCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level", nullable = false)
    private RescueUrgencyLevel urgencyLevel;

    @Column(name = "condition_tags", columnDefinition = "JSON")
    private String conditionTags;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "contact_mobile", nullable = false)
    private String contactMobile;

    @Column(name = "contact_mobile_masked", nullable = false)
    private String contactMobileMasked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RescueClueStatus status;

    @Column(name = "triage_note")
    private String triageNote;

    @Column(name = "resolution_note")
    private String resolutionNote;

    @Column(name = "handled_by_admin_id")
    private Long handledByAdminId;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Column(name = "suggested_resource_ids", columnDefinition = "JSON")
    private String suggestedResourceIds;

    public Long getId() {
        return id;
    }

    public String getClueNo() {
        return clueNo;
    }

    public void setClueNo(String clueNo) {
        this.clueNo = clueNo;
    }

    public Long getReporterUserId() {
        return reporterUserId;
    }

    public void setReporterUserId(Long reporterUserId) {
        this.reporterUserId = reporterUserId;
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

    public RescuePetType getPetType() {
        return petType;
    }

    public void setPetType(RescuePetType petType) {
        this.petType = petType;
    }

    public Integer getEstimatedCount() {
        return estimatedCount;
    }

    public void setEstimatedCount(Integer estimatedCount) {
        this.estimatedCount = estimatedCount;
    }

    public RescueUrgencyLevel getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(RescueUrgencyLevel urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getConditionTags() {
        return conditionTags;
    }

    public void setConditionTags(String conditionTags) {
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

    public String getContactMobileMasked() {
        return contactMobileMasked;
    }

    public void setContactMobileMasked(String contactMobileMasked) {
        this.contactMobileMasked = contactMobileMasked;
    }

    public RescueClueStatus getStatus() {
        return status;
    }

    public void setStatus(RescueClueStatus status) {
        this.status = status;
    }

    public String getTriageNote() {
        return triageNote;
    }

    public void setTriageNote(String triageNote) {
        this.triageNote = triageNote;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    public Long getHandledByAdminId() {
        return handledByAdminId;
    }

    public void setHandledByAdminId(Long handledByAdminId) {
        this.handledByAdminId = handledByAdminId;
    }

    public LocalDateTime getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }

    public String getSuggestedResourceIds() {
        return suggestedResourceIds;
    }

    public void setSuggestedResourceIds(String suggestedResourceIds) {
        this.suggestedResourceIds = suggestedResourceIds;
    }
}
