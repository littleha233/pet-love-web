package com.petlove.weblove.modules.feeding.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.feeding.enums.FeedingVisitStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "feeding_order_visits",
    uniqueConstraints = @UniqueConstraint(name = "uk_feeding_order_visits_order_index", columnNames = {"order_id", "visit_index"})
)
public class FeedingOrderVisit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "visit_index", nullable = false)
    private Integer visitIndex;

    @Column(name = "planned_start_at", nullable = false)
    private LocalDateTime plannedStartAt;

    @Column(name = "planned_end_at", nullable = false)
    private LocalDateTime plannedEndAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedingVisitStatus status;

    @Column(name = "actual_start_at")
    private LocalDateTime actualStartAt;

    @Column(name = "actual_end_at")
    private LocalDateTime actualEndAt;

    @Column(name = "food_done", nullable = false)
    private boolean foodDone;

    @Column(name = "water_done", nullable = false)
    private boolean waterDone;

    @Column(name = "litter_done", nullable = false)
    private boolean litterDone;

    @Column(name = "play_done", nullable = false)
    private boolean playDone;

    @Column(name = "health_observation")
    private String healthObservation;

    @Column(name = "visit_note", columnDefinition = "TEXT")
    private String visitNote;

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getVisitIndex() {
        return visitIndex;
    }

    public void setVisitIndex(Integer visitIndex) {
        this.visitIndex = visitIndex;
    }

    public LocalDateTime getPlannedStartAt() {
        return plannedStartAt;
    }

    public void setPlannedStartAt(LocalDateTime plannedStartAt) {
        this.plannedStartAt = plannedStartAt;
    }

    public LocalDateTime getPlannedEndAt() {
        return plannedEndAt;
    }

    public void setPlannedEndAt(LocalDateTime plannedEndAt) {
        this.plannedEndAt = plannedEndAt;
    }

    public FeedingVisitStatus getStatus() {
        return status;
    }

    public void setStatus(FeedingVisitStatus status) {
        this.status = status;
    }

    public LocalDateTime getActualStartAt() {
        return actualStartAt;
    }

    public void setActualStartAt(LocalDateTime actualStartAt) {
        this.actualStartAt = actualStartAt;
    }

    public LocalDateTime getActualEndAt() {
        return actualEndAt;
    }

    public void setActualEndAt(LocalDateTime actualEndAt) {
        this.actualEndAt = actualEndAt;
    }

    public boolean isFoodDone() {
        return foodDone;
    }

    public void setFoodDone(boolean foodDone) {
        this.foodDone = foodDone;
    }

    public boolean isWaterDone() {
        return waterDone;
    }

    public void setWaterDone(boolean waterDone) {
        this.waterDone = waterDone;
    }

    public boolean isLitterDone() {
        return litterDone;
    }

    public void setLitterDone(boolean litterDone) {
        this.litterDone = litterDone;
    }

    public boolean isPlayDone() {
        return playDone;
    }

    public void setPlayDone(boolean playDone) {
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
}
