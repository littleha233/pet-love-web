package com.petlove.weblove.modules.ops.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.ops.enums.BlacklistActionMode;
import com.petlove.weblove.modules.ops.enums.BlacklistScopeType;
import com.petlove.weblove.modules.ops.enums.BlacklistStatus;
import com.petlove.weblove.modules.ops.enums.BlacklistSubjectType;
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
@Table(name = "risk_blacklist_entries")
public class RiskBlacklistEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false)
    private BlacklistSubjectType subjectType;

    @Column(name = "subject_value", nullable = false)
    private String subjectValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    private BlacklistScopeType scopeType;

    @Column(name = "scope_value", nullable = false)
    private String scopeValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_mode", nullable = false)
    private BlacklistActionMode actionMode;

    @Column(name = "reason_code", nullable = false)
    private String reasonCode;

    @Column(name = "reason_note")
    private String reasonNote;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlacklistStatus status;

    @Column(name = "created_by_admin_id")
    private Long createdByAdminId;

    @Column(name = "updated_by_admin_id")
    private Long updatedByAdminId;

    public Long getId() {
        return id;
    }

    public BlacklistSubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(BlacklistSubjectType subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectValue() {
        return subjectValue;
    }

    public void setSubjectValue(String subjectValue) {
        this.subjectValue = subjectValue;
    }

    public BlacklistScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(BlacklistScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeValue() {
        return scopeValue;
    }

    public void setScopeValue(String scopeValue) {
        this.scopeValue = scopeValue;
    }

    public BlacklistActionMode getActionMode() {
        return actionMode;
    }

    public void setActionMode(BlacklistActionMode actionMode) {
        this.actionMode = actionMode;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonNote() {
        return reasonNote;
    }

    public void setReasonNote(String reasonNote) {
        this.reasonNote = reasonNote;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public BlacklistStatus getStatus() {
        return status;
    }

    public void setStatus(BlacklistStatus status) {
        this.status = status;
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
