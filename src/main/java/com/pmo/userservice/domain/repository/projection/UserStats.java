package com.pmo.userservice.domain.repository.projection;

import com.pmo.userservice.infrastructure.enums.RegistrationStatus;

public interface UserStats {

    RegistrationStatus getRegistrationStatus();

    int getNoOfUsers();
}
