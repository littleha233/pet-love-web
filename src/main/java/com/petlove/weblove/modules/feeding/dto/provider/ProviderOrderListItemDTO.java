package com.petlove.weblove.modules.feeding.dto.provider;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProviderOrderListItemDTO(
    Long orderId,
    String orderNo,
    String status,
    Long ownerUserId,
    String ownerNickname,
    String serviceCityName,
    Integer visitCount,
    BigDecimal quotedTotalAmount,
    LocalDateTime nextVisitPlannedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
