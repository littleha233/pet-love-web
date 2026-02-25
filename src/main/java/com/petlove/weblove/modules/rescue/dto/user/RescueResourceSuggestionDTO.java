package com.petlove.weblove.modules.rescue.dto.user;

public record RescueResourceSuggestionDTO(
    Long resourceId,
    String name,
    String resourceType,
    String contactPhone
) {
}
