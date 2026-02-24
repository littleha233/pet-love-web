package com.petlove.weblove.modules.feeding.entity;

import com.petlove.weblove.common.util.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "feeding_order_reviews")
public class FeedingOrderReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "provider_user_id", nullable = false)
    private Long providerUserId;

    @Column(name = "rating_overall", nullable = false)
    private Integer ratingOverall;

    @Column(name = "rating_timeliness")
    private Integer ratingTimeliness;

    @Column(name = "rating_cleanliness")
    private Integer ratingCleanliness;

    @Column(name = "rating_attitude")
    private Integer ratingAttitude;

    @Column(name = "content")
    private String content;

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public Long getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(Long providerUserId) {
        this.providerUserId = providerUserId;
    }

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
