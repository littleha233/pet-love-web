package com.petlove.weblove.modules.adoption.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.adoption.enums.AdoptionApplicationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "adoption_applications",
    uniqueConstraints = @UniqueConstraint(name = "uk_post_applicant", columnNames = {"post_id", "applicant_user_id"})
)
public class AdoptionApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "applicant_user_id", nullable = false)
    private Long applicantUserId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "living_env_note", columnDefinition = "TEXT")
    private String livingEnvNote;

    @Column(name = "pet_experience_note", columnDefinition = "TEXT")
    private String petExperienceNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdoptionApplicationStatus status;

    @Column(name = "handled_by_user_id")
    private Long handledByUserId;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Column(name = "decision_note")
    private String decisionNote;

    public Long getId() {
        return id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getApplicantUserId() {
        return applicantUserId;
    }

    public void setApplicantUserId(Long applicantUserId) {
        this.applicantUserId = applicantUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLivingEnvNote() {
        return livingEnvNote;
    }

    public void setLivingEnvNote(String livingEnvNote) {
        this.livingEnvNote = livingEnvNote;
    }

    public String getPetExperienceNote() {
        return petExperienceNote;
    }

    public void setPetExperienceNote(String petExperienceNote) {
        this.petExperienceNote = petExperienceNote;
    }

    public AdoptionApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(AdoptionApplicationStatus status) {
        this.status = status;
    }

    public Long getHandledByUserId() {
        return handledByUserId;
    }

    public void setHandledByUserId(Long handledByUserId) {
        this.handledByUserId = handledByUserId;
    }

    public LocalDateTime getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }
}
