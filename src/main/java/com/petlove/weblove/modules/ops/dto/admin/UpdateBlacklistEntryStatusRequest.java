package com.petlove.weblove.modules.ops.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateBlacklistEntryStatusRequest {

    @NotBlank
    @Size(max = 16)
    private String status;

    @Size(max = 255)
    private String reasonNote;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReasonNote() {
        return reasonNote;
    }

    public void setReasonNote(String reasonNote) {
        this.reasonNote = reasonNote;
    }
}
