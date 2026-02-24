package com.petlove.weblove.modules.feeding.dto.user;

import java.math.BigDecimal;
import java.util.List;

public record FeedingProviderDetailDTO(
    Long providerUserId,
    Long providerProfileId,
    String status,
    String displayName,
    String headline,
    String intro,
    String avatarUrl,
    String serviceCityCode,
    String serviceCityName,
    List<String> serviceDistricts,
    List<String> servicePetTypes,
    List<String> serviceItemTags,
    BigDecimal basePricePerVisit,
    Integer experienceYears,
    String acceptNotes,
    BigDecimal ratingAvg,
    Integer ratingCount,
    Integer completedOrderCount,
    FeedingProviderViewerContextDTO viewerContext
) {
}
