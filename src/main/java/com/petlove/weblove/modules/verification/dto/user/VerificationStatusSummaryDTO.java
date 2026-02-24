package com.petlove.weblove.modules.verification.dto.user;

import java.time.LocalDateTime;

public record VerificationStatusSummaryDTO(
    String status,
    Integer submitVersion,
    String rejectReasonText,
    LocalDateTime updatedAt
) {
}
