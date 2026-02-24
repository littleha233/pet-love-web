package com.petlove.weblove.modules.adoption.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public record RehomePostListItemDTO(
    long postId,
    String title,
    String cityCode,
    String cityName,
    String districtName,
    String petType,
    String petName,
    String petGender,
    Integer ageMonths,
    String breed,
    String coverImageUrl,
    List<String> temperamentTags,
    LocalDateTime publishedAt,
    int viewCount
) {
}
