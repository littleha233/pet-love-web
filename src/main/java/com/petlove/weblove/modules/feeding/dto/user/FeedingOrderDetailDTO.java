package com.petlove.weblove.modules.feeding.dto.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FeedingOrderDetailDTO(
    Long orderId,
    String orderNo,
    String status,
    Long ownerUserId,
    String ownerNickname,
    Long providerUserId,
    String providerDisplayName,
    String providerAvatarUrl,
    BigDecimal providerRatingAvg,
    String serviceCityCode,
    String serviceCityName,
    String serviceDistrictName,
    String serviceAddressDetail,
    String serviceAddressNote,
    String contactName,
    String contactMobileMasked,
    List<String> serviceItemTags,
    String ownerNote,
    BigDecimal requestedTotalAmount,
    BigDecimal quotedTotalAmount,
    String currency,
    List<FeedingOrderPetSnapshotDTO> pets,
    List<FeedingOrderVisitDTO> visits,
    FeedingReviewDTO review,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    FeedingOrderViewerContextDTO viewerContext
) {
}
