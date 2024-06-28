package com.pmo.userservice.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessType {

    ENTERPRISE("ENTERPRISE", "Enterprise"),
    WEB("WEB", "Web"),
    B2B("B2B", "b2b");

    private final String code;
    private final String title;
}
