package com.petlove.weblove.modules.rescue.dto.admin;

import com.petlove.weblove.modules.rescue.dto.user.RescueCluePhotoDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminRescueClueDetailDTO(
    Long clueId,
    String clueNo,
    Long reporterUserId,
    String reporterNickname,
    String cityCode,
    String cityName,
    String districtName,
    String locationText,
    BigDecimal geoLat,
    BigDecimal geoLng,
    String petType,
    Integer estimatedCount,
    String urgencyLevel,
    List<String> conditionTags,
    String description,
    String contactName,
    String contactMobile,
    String status,
    String triageNote,
    String resolutionNote,
    List<Long> suggestedResourceIds,
    List<RescueCluePhotoDTO> photos,
    String handledByAdminName,
    LocalDateTime handledAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
