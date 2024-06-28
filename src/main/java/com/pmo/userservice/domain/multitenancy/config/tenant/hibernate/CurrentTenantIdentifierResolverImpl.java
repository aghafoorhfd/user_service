package com.pmo.userservice.domain.multitenancy.config.tenant.hibernate;

import com.pmo.userservice.domain.multitenancy.domain.entity.TenantInfo;
import com.pmo.userservice.domain.multitenancy.util.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("currentTenantIdentifierResolver")
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String companyName = "BOOTSTRAP";
        TenantInfo tenantInfo = TenantContext.getTenantInfo();
        if (!ObjectUtils.isEmpty(tenantInfo)) {
            companyName = tenantInfo.getCompanyName();
        }
        return companyName;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
