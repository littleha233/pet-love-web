package com.petlove.weblove.modules.verification.dto.user;

public record MyVerificationOverviewDTO(
    VerificationStatusSummaryDTO realName,
    VerificationStatusSummaryDTO provider,
    boolean canSubmitProvider
) {
}
