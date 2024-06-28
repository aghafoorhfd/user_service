package com.pmo.userservice.application.integration.mapper;

import com.pmo.userservice.application.dto.DeleteUsersDTO;
import com.pmo.userservice.application.integration.dto.AuthDeleteUserListDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthDeleteUsersMapper {

  AuthDeleteUsersMapper AUTH_DELETE_USERS_MAPPER = Mappers.getMapper(AuthDeleteUsersMapper.class);

  AuthDeleteUserListDTO mapToAuthDeleteUsersDTO(DeleteUsersDTO deleteUsers);

}
