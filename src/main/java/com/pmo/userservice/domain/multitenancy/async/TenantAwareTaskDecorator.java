package com.pmo.userservice.domain.multitenancy.async;

import com.pmo.userservice.domain.multitenancy.domain.entity.TenantInfo;
import com.pmo.userservice.domain.multitenancy.util.TenantContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

public class TenantAwareTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        TenantInfo tenantInfo = TenantContext.getTenantInfo();
        return () -> {
            try {
                TenantContext.setTenantInfo(tenantInfo);
                runnable.run();
            } finally {
                TenantContext.setTenantInfo(null);
            }
        };
    }
}