package com.pmo.userservice.domain.repository.projection;

import com.pmo.userservice.domain.model.Address;
import com.pmo.userservice.infrastructure.enums.AccessType;

import java.util.UUID;

public interface UserDetails {

    UUID getId();

    String getFirstName();

    String getLastName();

    String getEmail();

    AccessType getAccessType();

    Address getAddress();
}
