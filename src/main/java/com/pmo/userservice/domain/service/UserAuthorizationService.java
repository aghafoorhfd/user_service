package com.pmo.userservice.domain.service;


import com.pmo.userservice.domain.model.Privileges;
import com.pmo.userservice.domain.model.RolePrivileges;

import java.util.List;
import java.util.UUID;

public interface UserAuthorizationService {
    void createRolePrivilegesJson(List<RolePrivileges> rolePrivilegesList);

    Privileges getPrivilegesByRoleAndCompanyId(String userRole, UUID companyId);
}
