package com.petlove.weblove.modules.ops.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpsertBlacklistEntryRequest {

    @NotBlank
    @Size(max = 32)
    private String subjectType;

    @NotBlank
    @Size(max = 128)
    private String subjectValue;

    @NotBlank
    @Size(max = 32)
    private String scopeType;

    @NotBlank
    @Size(max = 64)
    private String scopeValue;

    @NotBlank
    @Size(max = 16)
    private String actionMode;

    @NotBlank
    @Size(max = 64)
    private String reasonCode;

    @Size(max = 255)
    private String reasonNote;

    @NotBlank
    @Size(max = 32)
    private String startAt;

    @Size(max = 32)
    private String endAt;

    @Size(max = 16)
    private String status;

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectValue() {
        return subjectValue;
    }

    public void setSubjectValue(String subjectValue) {
        this.subjectValue = subjectValue;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeValue() {
        return scopeValue;
    }

    public void setScopeValue(String scopeValue) {
        this.scopeValue = scopeValue;
    }

    public String getActionMode() {
        return actionMode;
    }

    public void setActionMode(String actionMode) {
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

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
