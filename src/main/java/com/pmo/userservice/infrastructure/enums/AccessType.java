package com.pmo.userservice.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum AccessType {

    SUPER_ADMIN("SUPER_ADMIN", "Super Admin"),
    ADMIN("ADMIN", "Admin"),
    PROJECT_MANAGER("PROJECT_MANAGER", "Project Manager"),
    RESOURCE_MANAGER("RESOURCE_MANAGER", "Resource Manager"),
    CONFLICT_MANAGER("CONFLICT_MANAGER", "Conflict Manager"),
    GENERAL_USER("GENERAL_USER", "General User"),
    EXECUTIVE("EXECUTIVE", "Executive");

    private final String code;
    private final String title;

    public static Stream<AccessType> stream() {
        return Stream.of(AccessType.values());
    }
}
