package com.petlove.weblove.modules.adoption.dto.user;

import java.time.LocalDateTime;

public record AdoptionApplicationDTO(
    long applicationId,
    long postId,
    ApplicantSummaryDTO applicant,
    String message,
    String livingEnvNote,
    String petExperienceNote,
    String status,
    LocalDateTime createdAt,
    LocalDateTime handledAt,
    String decisionNote
) {
}
