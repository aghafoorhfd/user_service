package com.pmo.userservice.domain.multitenancy.repository;

import com.pmo.userservice.domain.multitenancy.domain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByCompanyName(String companyName);

    List<Tenant> findAllByIsObsoleteFalse();
}
