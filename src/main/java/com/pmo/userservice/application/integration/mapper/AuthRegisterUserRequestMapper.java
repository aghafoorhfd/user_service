package com.pmo.userservice.application.integration.mapper;

import com.pmo.userservice.application.integration.dto.AuthRegisterUserRequestDTO;
import com.pmo.userservice.domain.model.Company;
import com.pmo.userservice.domain.model.User;
import com.pmo.userservice.infrastructure.enums.AccessType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthRegisterUserRequestMapper {

  AuthRegisterUserRequestMapper AUTH_REGISTER_USER_REQUEST_MAPPER
      = Mappers.getMapper(AuthRegisterUserRequestMapper.class);

  /**
   * Maps user, company and password related info to register user request body.
   *
   * @param user     users info
   * @param company  users company info
   * @param password users password
   * @return Request body to register user
   */
  @Mapping(source = "user.email", target = "userName")
  @Mapping(source = "password", target = "password")
  @Mapping(source = "user.firstName", target = "firstName")
  @Mapping(source = "user.lastName", target = "lastName")
  @Mapping(source = "company.id", target = "companyId")
  @Mapping(source = "company.name", target = "companyName")
  @Mapping(source = "user.email", target = "emailAddress")
  @Mapping(target = "accessType", expression = "java(mapStatus(user.getAccessType()))")
  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "supportUser", target = "supportUser")
  AuthRegisterUserRequestDTO mapToAuthRegisterUserRequest(User user, Company company, String password, boolean supportUser);

  default String mapStatus(AccessType accessType) {
    return accessType.getCode();
  }

}
