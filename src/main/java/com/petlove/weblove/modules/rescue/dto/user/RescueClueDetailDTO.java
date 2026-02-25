package com.petlove.weblove.modules.rescue.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public record RescueClueDetailDTO(
    Long clueId,
    String clueNo,
    String cityCode,
    String cityName,
    String districtName,
    String locationText,
    String petType,
    Integer estimatedCount,
    String urgencyLevel,
    List<String> conditionTags,
    String description,
    String contactName,
    String contactMobileMasked,
    String status,
    String triageNote,
    String resolutionNote,
    List<RescueResourceSuggestionDTO> suggestedResources,
    List<RescueCluePhotoDTO> photos,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
