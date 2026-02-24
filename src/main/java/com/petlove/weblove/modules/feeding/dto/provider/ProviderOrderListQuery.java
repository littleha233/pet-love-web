package com.petlove.weblove.modules.feeding.dto.provider;

public record ProviderOrderListQuery(
    Integer page,
    Integer pageSize,
    String status
) {
}
