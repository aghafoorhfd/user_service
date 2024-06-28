package com.pmo.userservice.domain.multitenancy.util;

import com.pmo.userservice.domain.multitenancy.domain.entity.TenantInfo;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public final class TenantContext {

    private static final InheritableThreadLocal<TenantInfo> currentTenant =
        new InheritableThreadLocal<>();

    public static void setTenantInfo(TenantInfo tenantInfo) {
        log.debug("Setting tenantId to " + tenantInfo.getCompanyName());
        currentTenant.set(tenantInfo);
    }

    public static TenantInfo getTenantInfo() {
        return currentTenant.get();
    }

    public static void clear(){
        log.debug("Removing current tenant: " + currentTenant.get().getCompanyName());
        currentTenant.remove();
    }
}