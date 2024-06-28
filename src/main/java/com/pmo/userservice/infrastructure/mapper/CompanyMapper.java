package com.pmo.userservice.infrastructure.mapper;

import com.pmo.userservice.application.dto.CompanyResponseDTO;
import com.pmo.userservice.domain.model.Address;
import com.pmo.userservice.domain.model.Company;
import com.pmo.userservice.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper
public interface CompanyMapper {

    CompanyMapper COMPANY_MAPPER = Mappers.getMapper(CompanyMapper.class);

    CompanyResponseDTO mapToCompanyResponseDTO(Company company);

    @Mapping(target = "usedLicenses", constant = "1")
    @Mapping(source = "address", target = "address")
    @Mapping(target = "name", source = "companyName")
    @Mapping(target = "organizationName", source = "organizationName")
    @Mapping(source = "planPackageId", target = "planPackageId")
    @Mapping(source = "totalLicenses", target = "totalLicenses")
    @Mapping(target = "companyType", source = "user.company.companyType")
    @Mapping(target = "id", source = "user.company.id")
    Company mapToCompany(User user, Address address, UUID planPackageId,
                         Integer totalLicenses, String companyName, String organizationName);
}
