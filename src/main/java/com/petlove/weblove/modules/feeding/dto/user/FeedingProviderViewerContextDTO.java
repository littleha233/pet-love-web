package com.petlove.weblove.modules.feeding.dto.user;

public record FeedingProviderViewerContextDTO(
    boolean canCreateOrder,
    String cannotCreateOrderReason
) {
}
