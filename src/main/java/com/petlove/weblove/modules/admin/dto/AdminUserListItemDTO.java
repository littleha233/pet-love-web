package com.petlove.weblove.modules.admin.dto;

public record AdminUserListItemDTO(
    long userId,
    String mobileMasked,
    String emailMasked,
    String status,
    String nickname,
    String cityName
) {
}
