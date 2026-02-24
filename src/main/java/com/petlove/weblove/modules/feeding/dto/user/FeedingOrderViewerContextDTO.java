package com.petlove.weblove.modules.feeding.dto.user;

public record FeedingOrderViewerContextDTO(
    boolean isOwner,
    boolean isProvider,
    boolean canCancel,
    boolean canConfirmComplete,
    boolean canReview
) {
}
