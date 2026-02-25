package com.petlove.weblove.modules.ops.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminReplyComplaintTicketRequest {

    @NotBlank
    @Size(max = 2000)
    private String content;

    private Boolean isInternalNote;

    @Size(max = 32)
    private String moveToStatus;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsInternalNote() {
        return isInternalNote;
    }

    public void setIsInternalNote(Boolean internalNote) {
        isInternalNote = internalNote;
    }

    public String getMoveToStatus() {
        return moveToStatus;
    }

    public void setMoveToStatus(String moveToStatus) {
        this.moveToStatus = moveToStatus;
    }
}
