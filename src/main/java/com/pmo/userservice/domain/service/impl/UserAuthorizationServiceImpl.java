package com.pmo.userservice.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmo.common.exception.ApplicationException;
import com.pmo.common.exception.UnAuthorizedException;
import com.pmo.common.util.PMOUtil;
import com.pmo.userservice.domain.model.Company;
import com.pmo.userservice.domain.model.Privileges;
import com.pmo.userservice.domain.model.PrivilegesDetails;
import com.pmo.userservice.domain.model.RolePrivileges;
import com.pmo.userservice.domain.repository.CompanyRepository;
import com.pmo.userservice.domain.repository.RolePrivilegesRepository;
import com.pmo.userservice.domain.service.UserAuthorizationService;
import com.pmo.userservice.infrastructure.enums.AccessType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.pmo.userservice.infrastructure.utils.Constants.ADMIN_SCREEN_URL;
import static com.pmo.userservice.infrastructure.utils.Constants.BILLING_SCREEN_URL;
import static com.pmo.userservice.infrastructure.utils.Constants.CARD_DETAILS_SCREEN_URL;
import static com.pmo.userservice.infrastructure.utils.Constants.COMPANY;
import static com.pmo.userservice.infrastructure.utils.Constants.COMPANY_OR_ROLE_NOT_FOUND_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.JSON_PARSING_EXCEPTION_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.PENDING_INVOICE_SCREEN_URL;
import static com.pmo.userservice.infrastructure.utils.Constants.SUBSCRIPTION_ENDED_ERROR_MESSAGE;

/**
 * Service for managing users authorization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthorizationServiceImpl implements UserAuthorizationService {

    private final RolePrivilegesRepository rolePrivilegesRepository;
    private final CompanyRepository companyRepository;

    /**
     * creates role privileges JSON
     *
     */
    @Override
    public void createRolePrivilegesJson(List<RolePrivileges> rolePrivilegesList) {
        List<RolePrivileges> rolePrivilegesExistingList = rolePrivilegesRepository.findAll();
        if (!CollectionUtils.isEmpty(rolePrivilegesExistingList)) {
            // updates privileges json in existing list of role privileges for the same company
            updateExistingRolePrivileges(rolePrivilegesExistingList, rolePrivilegesList);
        }
        rolePrivilegesRepository.saveAll(rolePrivilegesList);
    }

    @Override
    public Privileges getPrivilegesByRoleAndCompanyId(String userRole, UUID companyId) {
        Company company = PMOUtil.validateAndGetObject(companyRepository.findById(companyId), COMPANY);
        Privileges privileges;
        if (Objects.nonNull(company.getIsSubscriptionActive()) &&
                Boolean.TRUE.equals(company.getIsSubscriptionActive())) {
            RolePrivileges rolePrivileges = PMOUtil.validateAndGetObject(
                    rolePrivilegesRepository.findByRole(userRole),
                    COMPANY_OR_ROLE_NOT_FOUND_ERROR_MESSAGE);
            ObjectMapper objectMapper = new ObjectMapper();
            List<PrivilegesDetails> privilegesDetails;
            try {
                privilegesDetails = objectMapper.readValue(rolePrivileges.getPrivileges(), new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw new ApplicationException(JSON_PARSING_EXCEPTION_ERROR_MESSAGE);
            }
            privileges = Privileges.builder()
                    .privilegesDetails(privilegesDetails)
                    .build();
        } else if (AccessType.SUPER_ADMIN.getCode().equals(userRole)) {
            privileges = Privileges.builder()
                    .privilegesDetails(List.of(PrivilegesDetails.builder()
                            .screen(ADMIN_SCREEN_URL)
                            .build(), PrivilegesDetails.builder()
                            .screen(CARD_DETAILS_SCREEN_URL)
                            .build(), PrivilegesDetails.builder()
                            .screen(BILLING_SCREEN_URL)
                            .build(), PrivilegesDetails.builder()
                            .screen(PENDING_INVOICE_SCREEN_URL)
                            .build()))
                    .build();
        } else {
            log.error("Company has been deactivated and user with {} role does not have billing screens access", userRole);
            throw new UnAuthorizedException(SUBSCRIPTION_ENDED_ERROR_MESSAGE);
        }
        return privileges;

    }

    /**
     * Updates privileges json in existing list of role privileges for the same company
     *
     * @param rolePrivilegesExistingList list of role privileges already exists for a company
     * @param rolePrivilegesList         list of role privileges to be saved for the company
     */
    private void updateExistingRolePrivileges(List<RolePrivileges> rolePrivilegesExistingList,
                                              List<RolePrivileges> rolePrivilegesList) {

        for (RolePrivileges rolePrivilege : rolePrivilegesList) {
            rolePrivilegesExistingList.stream().filter(existingPrivileges ->
                    Objects.equals(rolePrivilege.getRole(), existingPrivileges.getRole()))
                    .forEach(existingPrivileges -> {
                        rolePrivilege.setId(existingPrivileges.getId());
                        rolePrivilege.setCreatedDate(existingPrivileges.getCreatedDate());
                        rolePrivilege.setCreatedBy(existingPrivileges.getCreatedBy());
                        rolePrivilege.setLastModifiedBy(existingPrivileges.getLastModifiedBy());
                        rolePrivilege.setLastModifiedDate(existingPrivileges.getLastModifiedDate());
                    });
        }
    }
}
