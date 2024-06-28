package com.pmo.userservice.infrastructure.mapper;

import com.pmo.userservice.application.dto.UserCategoryDTO;
import com.pmo.userservice.domain.repository.projection.UserDomain;
import com.pmo.userservice.domain.repository.projection.UserStats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserStatMapper {

    UserStatMapper USER_STAT_MAPPER = Mappers.getMapper(UserStatMapper.class);

    List<UserCategoryDTO> toUserCategoryDTO(List<UserStats> userStatsList);

    List<UserCategoryDTO> mapToUserCategoryDTO(List<UserDomain> userDomain);
}
