package com.petlove.weblove.modules.rescue.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.rescue.enums.RescueGuideScenarioCode;
import com.petlove.weblove.modules.rescue.enums.RescueGuideStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_guides")
public class RescueGuide extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_code", nullable = false)
    private RescueGuideScenarioCode scenarioCode;

    @Column(nullable = false)
    private String title;

    private String summary;

    @Column(name = "content_md", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String contentMd;

    @Column(name = "city_code")
    private String cityCode;

    @Column(columnDefinition = "JSON")
    private String tags;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RescueGuideStatus status;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_by_admin_id")
    private Long createdByAdminId;

    @Column(name = "updated_by_admin_id")
    private Long updatedByAdminId;

    public Long getId() {
        return id;
    }

    public RescueGuideScenarioCode getScenarioCode() {
        return scenarioCode;
    }

    public void setScenarioCode(RescueGuideScenarioCode scenarioCode) {
        this.scenarioCode = scenarioCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContentMd() {
        return contentMd;
    }

    public void setContentMd(String contentMd) {
        this.contentMd = contentMd;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public RescueGuideStatus getStatus() {
        return status;
    }

    public void setStatus(RescueGuideStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
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
