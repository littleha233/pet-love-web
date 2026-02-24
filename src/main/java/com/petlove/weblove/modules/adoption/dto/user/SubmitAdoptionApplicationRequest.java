package com.petlove.weblove.modules.adoption.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SubmitAdoptionApplicationRequest {

    @NotBlank
    @Size(max = 2000)
    private String message;

    @Size(max = 1000)
    private String livingEnvNote;

    @Size(max = 1000)
    private String petExperienceNote;

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
}
