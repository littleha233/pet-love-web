package com.petlove.weblove.modules.adoption.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminAdoptionPostDetailDTO(
    long postId,
    String title,
    String content,
    String status,
    int submitVersion,
    long publisherUserId,
    String publisherNickname,
    String publisherMobileMasked,
    String cityCode,
    String cityName,
    String districtName,
    String petType,
    String petName,
    String petGender,
    Integer ageMonths,
    String breed,
    BigDecimal weightKg,
    String neuteredStatus,
    String vaccinatedStatus,
    String healthNote,
    List<String> temperamentTags,
    String specialCareNote,
    List<String> petImages,
    int applicationCount,
    String rejectReasonCode,
    String rejectReasonText,
    String reviewedByAdminName,
    LocalDateTime reviewedAt,
    LocalDateTime publishedAt,
    LocalDateTime closedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
