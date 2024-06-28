package com.pmo.userservice.infrastructure.mapper;

import com.pmo.userservice.application.dto.CreateUserDTO;
import com.pmo.userservice.application.dto.UpdateUserDTO;
import com.pmo.userservice.application.dto.UserDTO;
import com.pmo.userservice.application.dto.UserDetailsDTO;
import com.pmo.userservice.application.dto.UserRegisterRequestDTO;
import com.pmo.userservice.domain.model.Company;
import com.pmo.userservice.domain.model.User;
import com.pmo.userservice.domain.repository.projection.UserDetails;
import com.pmo.userservice.infrastructure.enums.AccessType;
import com.pmo.userservice.infrastructure.enums.InvitationStatus;
import com.pmo.userservice.infrastructure.enums.RegistrationStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper
public interface UserMapper {

    UserMapper MAPPER = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "company.organizationName", target = "organizationName")
    @Mapping(source = "address.address1", target = "address")
    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "invite", target = "invitationStatus")
    @Mapping(source = "status", target = "registrationStatus")
    @Mapping(target = "accessTypeName", expression = "java(getAccessTypeTitle(user.getAccessType()))")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "email", target = "userName")
    @Mapping(source = "address.phone1", target = "phoneNumber")
    UserDTO mapToUserDTO(User user);

    @Mapping(source = "address.phone1", target = "phoneNumber")
    UserDetailsDTO mapToUserDetailsDTO(UserDetails userDetails);

    @Mapping(source = "address.phone1", target = "phoneNumber")
    UserDetailsDTO mapToUserDetailsDTO(User user);

    @Mapping(source = "companyType", target = "company.companyType")
    @Mapping(source = "companyId", target = "company.id")
    @Mapping(source = "organizationName", target = "company.name")
    User mapToUser(UserRegisterRequestDTO userRegisterRequestDTO);

    @Mapping(source = "company.address", target = "address")
    @Mapping(target = "isDelete", constant = "false")
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "company", target = "company")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    User mapToUser(User user, Company company);

    @Mapping(target = "isDelete", constant = "false")
    @Mapping(target = "status", expression = "java(registrationStatusPending())")
    @Mapping(target = "invite", expression = "java(invitationStatusSendInvitation())")
    @Mapping(source = "phoneNumber", target = "address.phone1")
    @Mapping(source = "companyId", target = "company.id")
    @Mapping(target = "hasPassword", constant = "false")
    User mapToUser(CreateUserDTO user);

    @Mapping(source = "companyId", target = "company.id")
    @Mapping(source = "user.phoneNumber", target = "address.phone1")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.accessType", target = "accessType")
    User mapToUser(UpdateUserDTO user, UUID companyId);

    default RegistrationStatus registrationStatusPending() {
        return RegistrationStatus.PENDING;
    }

    default InvitationStatus invitationStatusSendInvitation() {
        return InvitationStatus.SEND_INVITATION;
    }

    default String getAccessTypeTitle(AccessType accessType) {
        return accessType.getTitle();
    }

}
