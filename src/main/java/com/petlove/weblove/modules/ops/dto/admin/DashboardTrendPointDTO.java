package com.petlove.weblove.modules.ops.dto.admin;

import java.time.LocalDate;

public record DashboardTrendPointDTO(
    LocalDate date,
    Long value
) {
}
