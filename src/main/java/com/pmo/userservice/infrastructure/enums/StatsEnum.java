package com.pmo.userservice.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatsEnum {

    USER_STATS("USER_STATS", "User Stats"),
    PENDING_REQUEST_STATS("PENDING_REQUEST_STATS", "Pending Request Stats"),
    USER_PIE_CHART_STATS("USER_PIE_CHART_STATS","User Pie Chart stats");

    private final String code;
    private final String title;
}
