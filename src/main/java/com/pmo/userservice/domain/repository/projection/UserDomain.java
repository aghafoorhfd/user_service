package com.pmo.userservice.domain.repository.projection;

import com.pmo.userservice.infrastructure.enums.AccessType;

public interface UserDomain {

    AccessType getAccessType();

    int getNoOfUsers();

}
