package com.pmo.userservice.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Designation {

    TEAM_LEAD("TEAM_LEAD", "Team Lead");

    private final String code;
    private final String title;
}
