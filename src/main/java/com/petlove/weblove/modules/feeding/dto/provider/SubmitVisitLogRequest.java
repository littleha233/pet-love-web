package com.petlove.weblove.modules.feeding.dto.provider;

import jakarta.validation.constraints.Size;
import java.util.List;

public class SubmitVisitLogRequest {

    private Boolean foodDone;
    private Boolean waterDone;
    private Boolean litterDone;
    private Boolean playDone;

    @Size(max = 255)
    private String healthObservation;

    @Size(max = 2000)
    private String visitNote;

    @Size(max = 9)
    private List<Long> photoFileIds;

    public Boolean getFoodDone() {
        return foodDone;
    }

    public void setFoodDone(Boolean foodDone) {
        this.foodDone = foodDone;
    }

    public Boolean getWaterDone() {
        return waterDone;
    }

    public void setWaterDone(Boolean waterDone) {
        this.waterDone = waterDone;
    }

    public Boolean getLitterDone() {
        return litterDone;
    }

    public void setLitterDone(Boolean litterDone) {
        this.litterDone = litterDone;
    }

    public Boolean getPlayDone() {
        return playDone;
    }

    public void setPlayDone(Boolean playDone) {
        this.playDone = playDone;
    }

    public String getHealthObservation() {
        return healthObservation;
    }

    public void setHealthObservation(String healthObservation) {
        this.healthObservation = healthObservation;
    }

    public String getVisitNote() {
        return visitNote;
    }

    public void setVisitNote(String visitNote) {
        this.visitNote = visitNote;
    }

    public List<Long> getPhotoFileIds() {
        return photoFileIds;
    }

    public void setPhotoFileIds(List<Long> photoFileIds) {
        this.photoFileIds = photoFileIds;
    }
}
