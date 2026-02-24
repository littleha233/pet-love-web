package com.petlove.weblove.modules.feeding.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SubmitFeedingReviewRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer ratingOverall;

    @Min(1)
    @Max(5)
    private Integer ratingTimeliness;

    @Min(1)
    @Max(5)
    private Integer ratingCleanliness;

    @Min(1)
    @Max(5)
    private Integer ratingAttitude;

    @Size(max = 1000)
    private String content;

    public Integer getRatingOverall() {
        return ratingOverall;
    }

    public void setRatingOverall(Integer ratingOverall) {
        this.ratingOverall = ratingOverall;
    }

    public Integer getRatingTimeliness() {
        return ratingTimeliness;
    }

    public void setRatingTimeliness(Integer ratingTimeliness) {
        this.ratingTimeliness = ratingTimeliness;
    }

    public Integer getRatingCleanliness() {
        return ratingCleanliness;
    }

    public void setRatingCleanliness(Integer ratingCleanliness) {
        this.ratingCleanliness = ratingCleanliness;
    }

    public Integer getRatingAttitude() {
        return ratingAttitude;
    }

    public void setRatingAttitude(Integer ratingAttitude) {
        this.ratingAttitude = ratingAttitude;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
