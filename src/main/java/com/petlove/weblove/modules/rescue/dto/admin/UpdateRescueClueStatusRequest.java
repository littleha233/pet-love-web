package com.petlove.weblove.modules.rescue.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UpdateRescueClueStatusRequest {

    @NotBlank
    @Size(max = 32)
    private String status;

    @Size(max = 255)
    private String triageNote;

    @Size(max = 255)
    private String resolutionNote;

    @Size(max = 10)
    private List<Long> suggestedResourceIds;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTriageNote() {
        return triageNote;
    }

    public void setTriageNote(String triageNote) {
        this.triageNote = triageNote;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    public List<Long> getSuggestedResourceIds() {
        return suggestedResourceIds;
    }

    public void setSuggestedResourceIds(List<Long> suggestedResourceIds) {
        this.suggestedResourceIds = suggestedResourceIds;
    }
}
