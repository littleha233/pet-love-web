package com.petlove.weblove.modules.feeding.dto.user;

import java.math.BigDecimal;
import java.util.List;

public record FeedingProviderListItemDTO(
    Long providerUserId,
    Long providerProfileId,
    String displayName,
    String headline,
    String avatarUrl,
    String serviceCityCode,
    String serviceCityName,
    List<String> servicePetTypes,
    List<String> serviceItemTags,
    BigDecimal basePricePerVisit,
    BigDecimal ratingAvg,
    Integer ratingCount,
    Integer completedOrderCount
) {
}
