package com.petlove.weblove.modules.rescue.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

public record AdminRescueResourceDetailDTO(
    Long resourceId,
    String resourceType,
    String name,
    String cityCode,
    String cityName,
    String districtName,
    String address,
    String contactPhone,
    String contactWechat,
    String contactOther,
    String serviceHours,
    String serviceScope,
    List<String> acceptPetTypes,
    List<String> capabilityTags,
    String description,
    String sourceUrl,
    LocalDateTime verifiedAt,
    Integer sortOrder,
    String status,
    Long createdByAdminId,
    Long updatedByAdminId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
