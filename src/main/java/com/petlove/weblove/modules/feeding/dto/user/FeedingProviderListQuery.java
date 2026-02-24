package com.petlove.weblove.modules.feeding.dto.user;

public record FeedingProviderListQuery(
    Integer page,
    Integer pageSize,
    String cityCode,
    String petType,
    String keyword,
    String sortBy
) {
}
