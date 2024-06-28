package com.pmo.userservice.domain.repository;

import com.pmo.userservice.domain.model.Company;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

  /**
   * Method to find company by companyId and subscriptionId
   *
   * @param id             company UUID
   * @param subscriptionId subscription UUID
   * @return company info by companyId and subscriptionId
   */
  Optional<Company> findByIdAndSubscriptionId(UUID id, UUID subscriptionId);

  Optional<Company> findByName(String name);
}
