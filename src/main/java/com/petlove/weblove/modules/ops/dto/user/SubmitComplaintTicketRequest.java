package com.petlove.weblove.modules.ops.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class SubmitComplaintTicketRequest {

    @NotBlank
    @Size(max = 32)
    private String targetType;

    private Long targetId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 5000)
    private String content;

    @Size(max = 16)
    private String priority;

    @Size(max = 32)
    private String contactMobile;

    @Size(max = 9)
    private List<@NotNull Long> evidenceFileIds;

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getContactMobile() {
        return contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }

    public List<Long> getEvidenceFileIds() {
        return evidenceFileIds;
    }

    public void setEvidenceFileIds(List<Long> evidenceFileIds) {
        this.evidenceFileIds = evidenceFileIds;
    }
}
