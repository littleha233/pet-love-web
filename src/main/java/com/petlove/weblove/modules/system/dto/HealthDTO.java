package com.petlove.weblove.modules.system.dto;

public record HealthDTO(
    String app,
    String status,
    boolean db,
    boolean storage
) {
}
