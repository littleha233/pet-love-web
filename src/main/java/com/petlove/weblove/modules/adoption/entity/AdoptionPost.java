package com.petlove.weblove.modules.adoption.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.adoption.enums.AdoptionPostStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "adoption_posts")
public class AdoptionPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "publisher_user_id", nullable = false)
    private Long publisherUserId;

    @Column(name = "pet_id", nullable = false)
    private Long petId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "city_code", nullable = false)
    private String cityCode;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    @Column(name = "district_name")
    private String districtName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdoptionPostStatus status;

    @Column(name = "submit_version", nullable = false)
    private Integer submitVersion;

    @Column(name = "reject_reason_code")
    private String rejectReasonCode;

    @Column(name = "reject_reason_text")
    private String rejectReasonText;

    @Column(name = "reviewed_by_admin_id")
    private Long reviewedByAdminId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    public Long getId() {
        return id;
    }

    public Long getPublisherUserId() {
        return publisherUserId;
    }

    public void setPublisherUserId(Long publisherUserId) {
        this.publisherUserId = publisherUserId;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public AdoptionPostStatus getStatus() {
        return status;
    }

    public void setStatus(AdoptionPostStatus status) {
        this.status = status;
    }

    public Integer getSubmitVersion() {
        return submitVersion;
    }

    public void setSubmitVersion(Integer submitVersion) {
        this.submitVersion = submitVersion;
    }

    public String getRejectReasonCode() {
        return rejectReasonCode;
    }

    public void setRejectReasonCode(String rejectReasonCode) {
        this.rejectReasonCode = rejectReasonCode;
    }

    public String getRejectReasonText() {
        return rejectReasonText;
    }

    public void setRejectReasonText(String rejectReasonText) {
        this.rejectReasonText = rejectReasonText;
    }

    public Long getReviewedByAdminId() {
        return reviewedByAdminId;
    }

    public void setReviewedByAdminId(Long reviewedByAdminId) {
        this.reviewedByAdminId = reviewedByAdminId;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
}
