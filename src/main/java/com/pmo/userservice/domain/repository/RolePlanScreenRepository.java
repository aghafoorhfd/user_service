package com.pmo.userservice.domain.repository;

import com.pmo.userservice.domain.model.RolePlanScreen;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePlanScreenRepository extends JpaRepository<RolePlanScreen, UUID> {

  /**
   * Method to find list of role plan screen by planId.
   *
   * @param planId plan UUID
   * @return List of role plan screen
   */
  List<RolePlanScreen> findAllByPlanId(UUID planId);
}
