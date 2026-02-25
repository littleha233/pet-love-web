package com.petlove.weblove.modules.ops.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.ops.enums.CityFeatureKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "city_feature_switches")
public class CityFeatureSwitch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_code", nullable = false)
    private String cityCode;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_key", nullable = false)
    private CityFeatureKey featureKey;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    @Column(name = "allow_read", nullable = false)
    private boolean allowRead;

    @Column(name = "allow_write", nullable = false)
    private boolean allowWrite;

    @Column(name = "notice_text")
    private String noticeText;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "updated_by_admin_id")
    private Long updatedByAdminId;

    public Long getId() {
        return id;
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

    public CityFeatureKey getFeatureKey() {
        return featureKey;
    }

    public void setFeatureKey(CityFeatureKey featureKey) {
        this.featureKey = featureKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAllowRead() {
        return allowRead;
    }

    public void setAllowRead(boolean allowRead) {
        this.allowRead = allowRead;
    }

    public boolean isAllowWrite() {
        return allowWrite;
    }

    public void setAllowWrite(boolean allowWrite) {
        this.allowWrite = allowWrite;
    }

    public String getNoticeText() {
        return noticeText;
    }

    public void setNoticeText(String noticeText) {
        this.noticeText = noticeText;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Long getUpdatedByAdminId() {
        return updatedByAdminId;
    }

    public void setUpdatedByAdminId(Long updatedByAdminId) {
        this.updatedByAdminId = updatedByAdminId;
    }
}
