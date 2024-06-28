package com.pmo.userservice.domain.service;

import com.pmo.userservice.domain.multitenancy.domain.entity.Tenant;

public interface TenantService {

    Tenant getTenantByCompanyName(String companyName);
}
