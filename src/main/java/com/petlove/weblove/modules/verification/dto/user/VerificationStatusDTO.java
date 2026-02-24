package com.petlove.weblove.modules.verification.dto.user;

import java.time.LocalDateTime;

public record VerificationStatusDTO(
    String verificationType,
    String status,
    int submitVersion,
    LocalDateTime updatedAt
) {
}
