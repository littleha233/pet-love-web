package com.petlove.weblove.modules.feeding.dto.user;

import jakarta.validation.constraints.NotBlank;

public class CreateFeedingOrderVisitDTO {

    @NotBlank
    private String plannedStartAt;

    @NotBlank
    private String plannedEndAt;

    public String getPlannedStartAt() {
        return plannedStartAt;
    }

    public void setPlannedStartAt(String plannedStartAt) {
        this.plannedStartAt = plannedStartAt;
    }

    public String getPlannedEndAt() {
        return plannedEndAt;
    }

    public void setPlannedEndAt(String plannedEndAt) {
        this.plannedEndAt = plannedEndAt;
    }
}
