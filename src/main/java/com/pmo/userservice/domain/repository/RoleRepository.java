package com.pmo.userservice.domain.repository;

import com.pmo.userservice.domain.model.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {

  /**
   * Method finds a role by access type.
   *
   * @param accessType there are multiple access type as per the user role
   * @return Roles info
   */
  Optional<Role> findByName(String accessType);
}
