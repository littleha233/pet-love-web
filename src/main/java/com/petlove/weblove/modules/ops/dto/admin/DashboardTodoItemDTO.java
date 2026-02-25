package com.petlove.weblove.modules.ops.dto.admin;

public record DashboardTodoItemDTO(
    String todoType,
    String title,
    Long count,
    String priority,
    String targetRoute
) {
}
