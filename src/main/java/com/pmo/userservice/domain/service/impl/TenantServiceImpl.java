package com.pmo.userservice.domain.service.impl;

import com.pmo.userservice.domain.multitenancy.domain.entity.Tenant;
import com.pmo.userservice.domain.multitenancy.repository.TenantRepository;
import com.pmo.userservice.domain.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    public Tenant getTenantByCompanyName(String companyName) {

        Optional<Tenant> tenant = tenantRepository.findByCompanyName(companyName);
        return tenant.orElse(null);
    }
}
