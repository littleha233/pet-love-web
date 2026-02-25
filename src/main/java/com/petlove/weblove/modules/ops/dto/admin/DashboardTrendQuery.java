package com.petlove.weblove.modules.ops.dto.admin;

import jakarta.validation.constraints.NotBlank;

public class DashboardTrendQuery {

    @NotBlank
    private String metric;

    private Integer days = 7;

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
}
