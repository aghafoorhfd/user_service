package com.pmo.userservice.domain.multitenancy.domain.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import lombok.Getter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class TenantInfo {

    private String companyName;
    private Boolean isDatabaseCreationAllowed;
}
