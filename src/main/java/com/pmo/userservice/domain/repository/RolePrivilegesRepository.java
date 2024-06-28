package com.pmo.userservice.domain.repository;

import com.pmo.userservice.domain.model.RolePrivileges;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePrivilegesRepository extends JpaRepository<RolePrivileges, UUID> {

  /**
   * Method to find all the list role privileges by companyId.
   *
   * @param companyId company UUID
   * @return list of role privileges
   */
  List<RolePrivileges> findAllByCompanyId(UUID companyId);

  /**
   * Method to find a role privilege by companyId and role.
   *
   * @param role      user appropriate role
   * @return Role     privilege info
   */
  Optional<RolePrivileges> findByRole(String role);
}
