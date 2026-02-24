package com.petlove.weblove.modules.adoption.dto.user;

public record ViewerContextDTO(
    boolean hasApplied,
    boolean canApply,
    String cannotApplyReason
) {
}
