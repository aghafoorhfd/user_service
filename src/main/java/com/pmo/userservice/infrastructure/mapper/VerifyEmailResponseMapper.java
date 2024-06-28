package com.pmo.userservice.infrastructure.mapper;

import com.pmo.userservice.application.dto.VerifyEmailResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper
public interface VerifyEmailResponseMapper {

    VerifyEmailResponseMapper VERIFY_EMAIL_RESPONSE_MAPPER = Mappers.getMapper(VerifyEmailResponseMapper.class);

    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "iamId", target = "iamId")
    VerifyEmailResponseDTO mapToVerifyEmailResponseDTO(UUID userId, UUID iamId);
}
