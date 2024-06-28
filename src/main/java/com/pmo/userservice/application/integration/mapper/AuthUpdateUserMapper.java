package com.pmo.userservice.application.integration.mapper;

import com.pmo.userservice.application.dto.UpdateUserDTO;
import com.pmo.userservice.application.integration.dto.AuthUpdateUserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper
public interface AuthUpdateUserMapper {

    AuthUpdateUserMapper AUTH_UPDATE_USER_MAPPER = Mappers.getMapper(AuthUpdateUserMapper.class);

    @Mapping(source = "userIAmId", target = "userIAmId")
    @Mapping(source = "companyName", target = "companyName")
    AuthUpdateUserDTO mapToAuthUpdateUser(UpdateUserDTO updateUser, UUID userIAmId, String companyName);
}
