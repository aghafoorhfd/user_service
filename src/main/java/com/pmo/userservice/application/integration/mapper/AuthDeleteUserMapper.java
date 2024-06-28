package com.pmo.userservice.application.integration.mapper;

import com.pmo.userservice.application.dto.DeleteUserDTO;
import com.pmo.userservice.application.integration.dto.AuthDeleteUserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthDeleteUserMapper {

  AuthDeleteUserMapper AUTH_DELETE_USER_MAPPER = Mappers.getMapper(AuthDeleteUserMapper.class);

  AuthDeleteUserDTO mapToAuthDeleteUserDTO(DeleteUserDTO deleteUser);

}
