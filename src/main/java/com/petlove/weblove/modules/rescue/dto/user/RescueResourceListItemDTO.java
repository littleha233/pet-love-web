package com.petlove.weblove.modules.rescue.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public record RescueResourceListItemDTO(
    Long resourceId,
    String resourceType,
    String name,
    String cityCode,
    String cityName,
    String districtName,
    String serviceScope,
    List<String> acceptPetTypes,
    List<String> capabilityTags,
    String contactPhoneMasked,
    LocalDateTime verifiedAt,
    Integer sortOrder
) {
}
