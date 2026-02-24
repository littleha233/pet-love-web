package com.petlove.weblove.modules.verification.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.verification.enums.VerificationStatus;
import com.petlove.weblove.modules.verification.enums.VerificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_verifications",
    uniqueConstraints = @UniqueConstraint(name = "uk_user_verifications_user_type", columnNames = {"user_id", "verification_type"})
)
public class UserVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", nullable = false)
    private VerificationType verificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    @Column(name = "submit_version", nullable = false)
    private Integer submitVersion;

    @Column(name = "real_name")
    private String realName;

    @Column(name = "id_no_masked")
    private String idNoMasked;

    @Column(name = "id_no_hash")
    private String idNoHash;

    @Column(name = "id_front_file_id")
    private Long idFrontFileId;

    @Column(name = "id_back_file_id")
    private Long idBackFileId;

    @Column(name = "holding_id_file_id")
    private Long holdingIdFileId;

    @Column(name = "provider_experience_years")
    private Integer providerExperienceYears;

    @Column(name = "provider_intro", columnDefinition = "TEXT")
    private String providerIntro;

    @Column(name = "provider_service_pet_types", columnDefinition = "JSON")
    private String providerServicePetTypes;

    @Column(name = "provider_service_city_code")
    private String providerServiceCityCode;

    @Column(name = "provider_capability_tags", columnDefinition = "JSON")
    private String providerCapabilityTags;

    @Column(name = "supporting_file_ids", columnDefinition = "JSON")
    private String supportingFileIds;

    @Column(name = "reject_reason_code")
    private String rejectReasonCode;

    @Column(name = "reject_reason_text")
    private String rejectReasonText;

    @Column(name = "reviewed_by_admin_id")
    private Long reviewedByAdminId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public VerificationType getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(VerificationType verificationType) {
        this.verificationType = verificationType;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public Integer getSubmitVersion() {
        return submitVersion;
    }

    public void setSubmitVersion(Integer submitVersion) {
        this.submitVersion = submitVersion;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdNoMasked() {
        return idNoMasked;
    }

    public void setIdNoMasked(String idNoMasked) {
        this.idNoMasked = idNoMasked;
    }

    public String getIdNoHash() {
        return idNoHash;
    }

    public void setIdNoHash(String idNoHash) {
        this.idNoHash = idNoHash;
    }

    public Long getIdFrontFileId() {
        return idFrontFileId;
    }

    public void setIdFrontFileId(Long idFrontFileId) {
        this.idFrontFileId = idFrontFileId;
    }

    public Long getIdBackFileId() {
        return idBackFileId;
    }

    public void setIdBackFileId(Long idBackFileId) {
        this.idBackFileId = idBackFileId;
    }

    public Long getHoldingIdFileId() {
        return holdingIdFileId;
    }

    public void setHoldingIdFileId(Long holdingIdFileId) {
        this.holdingIdFileId = holdingIdFileId;
    }

    public Integer getProviderExperienceYears() {
        return providerExperienceYears;
    }

    public void setProviderExperienceYears(Integer providerExperienceYears) {
        this.providerExperienceYears = providerExperienceYears;
    }

    public String getProviderIntro() {
        return providerIntro;
    }

    public void setProviderIntro(String providerIntro) {
        this.providerIntro = providerIntro;
    }

    public String getProviderServicePetTypes() {
        return providerServicePetTypes;
    }

    public void setProviderServicePetTypes(String providerServicePetTypes) {
        this.providerServicePetTypes = providerServicePetTypes;
    }

    public String getProviderServiceCityCode() {
        return providerServiceCityCode;
    }

    public void setProviderServiceCityCode(String providerServiceCityCode) {
        this.providerServiceCityCode = providerServiceCityCode;
    }

    public String getProviderCapabilityTags() {
        return providerCapabilityTags;
    }

    public void setProviderCapabilityTags(String providerCapabilityTags) {
        this.providerCapabilityTags = providerCapabilityTags;
    }

    public String getSupportingFileIds() {
        return supportingFileIds;
    }

    public void setSupportingFileIds(String supportingFileIds) {
        this.supportingFileIds = supportingFileIds;
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
}
