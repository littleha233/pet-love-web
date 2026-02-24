package com.petlove.weblove.modules.admin.dto;

public record AdminUserDTO(
    long id,
    String username,
    String displayName,
    String role,
    String status
) {
}
