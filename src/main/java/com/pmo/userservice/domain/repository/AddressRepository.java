package com.pmo.userservice.domain.repository;

import com.pmo.userservice.domain.model.Address;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, UUID> {

}
