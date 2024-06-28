package com.pmo.userservice.infrastructure.mapper;

import com.pmo.userservice.application.dto.PrivilegesDTO;
import com.pmo.userservice.domain.model.Privileges;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RolePrivilegesMapper {

    RolePrivilegesMapper ROLE_PRIVILEGES_MAPPER = Mappers.getMapper(RolePrivilegesMapper.class);

    PrivilegesDTO mapToPrivilegesDTO(Privileges privileges);

}
