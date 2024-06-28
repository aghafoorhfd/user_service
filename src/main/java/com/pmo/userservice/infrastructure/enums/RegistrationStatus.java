package com.pmo.userservice.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RegistrationStatus {

    ACTIVE("ACTIVE", "Active"),
    IN_ACTIVE("IN_ACTIVE", "In Active"),
    PENDING("PENDING", "Pending"),
    REJECTED("REJECTED", "Rejected"),
    REVOKED("REVOKED", "Revoked");

    private final String code;
    private final String title;
}
