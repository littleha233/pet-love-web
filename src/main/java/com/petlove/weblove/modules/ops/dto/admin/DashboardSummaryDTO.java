package com.petlove.weblove.modules.ops.dto.admin;

import java.time.LocalDateTime;

public record DashboardSummaryDTO(
    Long todayNewUsers,
    Long todayNewAdoptionPosts,
    Long todayNewFeedingOrders,
    Long todayNewRescueClues,
    Long todayNewComplaints,
    Long pendingVerificationCount,
    Long pendingAdoptionReviewCount,
    Long pendingComplaintCount,
    Long pendingRescueClueCount,
    Long activeFeedingProviders,
    Long publishedAdoptionPosts,
    Long activeRescueResources,
    LocalDateTime generatedAt
) {
}
