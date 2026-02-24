package com.petlove.weblove.modules.adoption.dto.user;

import com.petlove.weblove.modules.adoption.enums.AdoptionApplicationHandleAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class HandleAdoptionApplicationRequest {

    @NotNull
    private AdoptionApplicationHandleAction action;

    @Size(max = 255)
    private String decisionNote;

    public AdoptionApplicationHandleAction getAction() {
        return action;
    }

    public void setAction(AdoptionApplicationHandleAction action) {
        this.action = action;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }
}
