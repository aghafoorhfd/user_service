package com.pmo.userservice.application.integration.mapper;

import com.pmo.userservice.application.dto.EnableUserDTO;
import com.pmo.userservice.application.integration.dto.AuthEnableUserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthEnableUserMapper {

  AuthEnableUserMapper AUTH_ENABLE_USER_MAPPER = Mappers.getMapper(AuthEnableUserMapper.class);

  AuthEnableUserDTO mapToAuthEnableUserDTO(EnableUserDTO enableUser);

}
