package com.petlove.weblove.modules.feeding.dto.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyFeedingOrderListItemDTO(
    Long orderId,
    String orderNo,
    String status,
    Long providerUserId,
    String providerDisplayName,
    String providerAvatarUrl,
    String serviceCityName,
    Integer visitCount,
    BigDecimal quotedTotalAmount,
    LocalDateTime nextVisitPlannedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
