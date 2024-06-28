package com.pmo.userservice.application.service.impl;

import com.pmo.common.dto.EmailTemplateModelDTO;
import com.pmo.common.enums.PmoErrors;
import com.pmo.common.exception.ApplicationException;
import com.pmo.common.util.CommonValidationUtil;
import com.pmo.common.util.EmailUtils;
import com.pmo.common.util.PMOUtil;
import com.pmo.common.util.StringUtils;
import com.pmo.userservice.application.dto.CompanyResponseDTO;
import com.pmo.userservice.application.dto.CreateUserDTO;
import com.pmo.userservice.application.dto.DeleteUserDTO;
import com.pmo.userservice.application.dto.EnableUserDTO;
import com.pmo.userservice.application.dto.LoginRequestDTO;
import com.pmo.userservice.application.dto.LoginResponseDTO;
import com.pmo.userservice.application.dto.PrivilegesDTO;
import com.pmo.userservice.application.dto.UpdateSubscriptionDetailsDTO;
import com.pmo.userservice.application.dto.UpdateUserDTO;
import com.pmo.userservice.application.dto.UserCategoryDTO;
import com.pmo.userservice.application.dto.UserDTO;
import com.pmo.userservice.application.dto.UserDetailsDTO;
import com.pmo.userservice.application.dto.UserForgotPasswordRequestDTO;
import com.pmo.userservice.application.dto.UserRegisterRequestDTO;
import com.pmo.userservice.application.dto.VerifyEmailResponseDTO;
import com.pmo.userservice.application.integration.client.CloudFlareClient;
import com.pmo.userservice.application.integration.client.ResourceClient;
import com.pmo.userservice.application.integration.dto.AuthRegisterUserResponseDTO;
import com.pmo.userservice.application.integration.dto.EmailContentDTO;
import com.pmo.userservice.application.integration.dto.ResourceUpdateRequestDTO;
import com.pmo.userservice.application.integration.service.AuthService;
import com.pmo.userservice.application.integration.service.MessagePublisher;
import com.pmo.userservice.application.integration.service.impl.CustomerQueueJmsPublisher;
import com.pmo.userservice.application.service.ApplicationService;
import com.pmo.userservice.domain.model.Address;
import com.pmo.userservice.domain.model.Company;
import com.pmo.userservice.domain.model.User;
import com.pmo.userservice.domain.model.VerificationToken;
import com.pmo.userservice.domain.repository.projection.UserDetails;
import com.pmo.userservice.domain.repository.projection.UserDomain;
import com.pmo.userservice.domain.repository.projection.UserStats;
import com.pmo.userservice.domain.service.TokenService;
import com.pmo.userservice.domain.service.UserAuthorizationService;
import com.pmo.userservice.domain.service.UserService;
import com.pmo.userservice.infrastructure.enums.AccessType;
import com.pmo.userservice.infrastructure.enums.BusinessType;
import com.pmo.userservice.infrastructure.enums.InvitationStatus;
import com.pmo.userservice.infrastructure.enums.RegistrationStatus;
import com.pmo.userservice.infrastructure.enums.StatsEnum;
import com.pmo.userservice.infrastructure.mapper.VerifyEmailResponseMapper;
import com.pmo.userservice.infrastructure.utils.Constants;
import com.pmo.userservice.infrastructure.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.pmo.userservice.application.integration.utils.Constants.BEARER;
import static com.pmo.userservice.application.integration.utils.Constants.DECISION_PENDING_ERROR_MESSAGE;
import static com.pmo.userservice.application.integration.utils.Constants.INACTIVE_USER_ERROR_MESSAGE;
import static com.pmo.userservice.application.integration.utils.Constants.MIN_LICENSE_MESSAGE;
import static com.pmo.userservice.application.integration.utils.Constants.NO_SUBSCRIPTION_FOUND_ERROR_MESSAGE;
import static com.pmo.userservice.application.integration.utils.Constants.PENDING_USER_ERROR_MESSAGE;
import static com.pmo.userservice.application.integration.utils.Constants.REJECTED_USER_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.mapper.CompanyMapper.COMPANY_MAPPER;
import static com.pmo.userservice.infrastructure.mapper.RolePrivilegesMapper.ROLE_PRIVILEGES_MAPPER;
import static com.pmo.userservice.infrastructure.mapper.UserMapper.MAPPER;
import static com.pmo.userservice.infrastructure.mapper.UserStatMapper.USER_STAT_MAPPER;
import static com.pmo.userservice.infrastructure.utils.Constants.COMPANY;
import static com.pmo.userservice.infrastructure.utils.Constants.COMPANY_ID_OR_SUBSCRIPTION_ID_NOT_FOUND_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.COMPANY_NOT_UPDATED;
import static com.pmo.userservice.infrastructure.utils.Constants.DISABLED_USER_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.EMAIL_NOT_VERIFIED_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.INVALID_ACCESS_TYPE;
import static com.pmo.userservice.infrastructure.utils.Constants.INVALID_COMPANY_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.INVALID_REGISTRATION_STATUS_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.INVALID_USER;
import static com.pmo.userservice.infrastructure.utils.Constants.LICENSES_UTILIZED_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_ADD_B2B_CUSTOMER_TEMPLATE;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_PM_RM_INVITE_SUBJECT;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_PM_RM_INVITE_TEMPLATE;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_SET_PASSWORD_VERIFY_SUBJECT;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_SUBSCRIPTION_ENDED_SUBJECT;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_SUBSCRIPTION_ENDED_TEMPLATE;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_SUBSCRIPTION_REACTIVATED_SUBJECT;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_SUBSCRIPTION_REACTIVATED_TEMPLATE;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_USER_ACTIVATED_SUBJECT;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_USER_LICENSE_REVOKED_SUBJECT;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_USER_LICENSE_REVOKED_TEMPLATE;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_USER_REACTIVATED_TEMPLATE;
import static com.pmo.userservice.infrastructure.utils.Constants.NOT_REGISTERED_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.RENEW_SUBSCRIPTION;
import static com.pmo.userservice.infrastructure.utils.Constants.REVOKE_SUPER_ADMIN_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.STATS_DATA_NOT_AVAILABLE;
import static com.pmo.userservice.infrastructure.utils.Constants.SUPPORT;
import static com.pmo.userservice.infrastructure.utils.Constants.USER;
import static com.pmo.userservice.infrastructure.utils.Constants.USER_ALREADY_REJECTED_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.USER_DEACTIVATED_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.USER_NOT_AUTHORIZED_ERROR_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final UserService userService;
    private final TokenService tokenService;
    private final AuthService authService;
    private final UserUtils userUtils;
    private final MessagePublisher emailQueueJmsPublisher;
    private final CustomerQueueJmsPublisher customerQueueJmsPublisher;
    private final UserAuthorizationService userAuthorizationService;
    private final ResourceClient resourceClient;

    @Value("${front-end.base-url}")
    private String frontEndBaseUrl;

    @Value("${front-end.protocol}")
    private String protocol;

    @Value("${front-end.path.set-password}")
    private String frontEndSetPasswordPath;

    @Value("${cloudflare.access-token}")
    private String accessToken;

    @Value("${cloudflare.domain}")
    private String domain;

    @Value("${cloudflare.service.ttl}")
    private Integer ttl;

    @Value("${env.support.email}")
    private String supportUserEmail;
    @Value("${env.support.password}")
    private String supportUserPassword;

    private final CloudFlareClient cloudFlareClient;

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void registerUser(UserRegisterRequestDTO userRegisterRequest, UUID planPackageId, int totalLicenses,
                             String companyName) {
        log.info("Registering user having email: {}", userRegisterRequest.getEmail());
        CommonValidationUtil.isTrue(totalLicenses >= 10, MIN_LICENSE_MESSAGE);
        User user = userService.registerUser(MAPPER.mapToUser(userRegisterRequest), planPackageId,
                totalLicenses, companyName);
        Map<String, String> emailContent = tokenService.getRegisterUserInviteEmailContent(user);
        emailQueueJmsPublisher.sendMessage(emailContent);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void registerEnterpriseUser(UserRegisterRequestDTO userRegisterRequest, EmailContentDTO emailContent,
                                       UpdateSubscriptionDetailsDTO updateSubscriptionDetails) {
        log.info("Registering user having email: {}", userRegisterRequest.getEmail());
        User user = userService.registerUser(MAPPER.mapToUser(userRegisterRequest), null, 0,
                userRegisterRequest.getOrganizationName());
        this.updateSubscriptionDetails(updateSubscriptionDetails);
        VerificationToken verificationToken = tokenService.generateVerificationToken(user);
        String frontEndSetPasswordAbsolutePath = MessageFormat.format(frontEndSetPasswordPath,
                user.getCompany().getName(), verificationToken.getToken().toString());
        String verificationUrl = UserUtils.generateVerificationUrl(protocol, user.getCompany().getName(),
                frontEndBaseUrl, frontEndSetPasswordAbsolutePath);

        EmailTemplateModelDTO model = EmailTemplateModelDTO.builder()
                .companyName(user.getCompany().getName())
                .currencyCode(emailContent.getCurrencyCode())
                .trialPeriod(emailContent.getTrialPeriod())
                .totalAmountPerCycle(emailContent.getTotalAmountPerCycle())
                .totalAmountToBeCharged(emailContent.getTotalAmountTobeCharged())
                .currentCycleStartDate(emailContent.getCurrentCycleStartDate())
                .currentCycleEndDate(emailContent.getCurrentCycleEndDate())
                .totalLicenses(user.getCompany().getTotalLicenses())
                .verificationUrl(verificationUrl)
                .build();

        emailQueueJmsPublisher.sendMessage(
                EmailUtils.generateEmailNotificationObject(
                        user.getEmail(),
                        null,
                        null,
                        MAIL_MESSAGE_SET_PASSWORD_VERIFY_SUBJECT,
                        MAIL_MESSAGE_ADD_B2B_CUSTOMER_TEMPLATE,
                        model
                ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateResendInviteVerificationURL(String email) {
        log.info("Resending invite verification to customer having email: {}", email);
        String verificationUrl = null;
        User user = userService.getUserByEmail(email);
        if (PMOUtil.isNotNull(user) && (PMOUtil.isNull(user.getHasPassword()) || !user.getHasPassword())) {
            VerificationToken verificationToken = tokenService.generateVerificationToken(user);
            String frontEndSetPasswordAbsolutePath = MessageFormat.format(frontEndSetPasswordPath,
                user.getCompany().getName(), String.valueOf(verificationToken.getToken()));
            verificationUrl = UserUtils.generateVerificationUrl(protocol, user.getCompany().getName(),
                frontEndBaseUrl, frontEndSetPasswordAbsolutePath);
        }
        return verificationUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public UserDetailsDTO addUser(CreateUserDTO createUser, String accessType) {
        log.info("addUser, accessType: {} , createUser.companyId: {}", accessType,
                createUser.getCompanyId());
        validateUserCreationAccess(createUser.getAccessType().getCode(), accessType);
        if (createUser.getCompanyId() == null) {
            log.error("An error occurred while creating user. {}", INVALID_COMPANY_MESSAGE);
            throw new IllegalArgumentException(INVALID_COMPANY_MESSAGE);
        }
        log.info("Fetching user company by companyId. {}", createUser.getCompanyId());
        Optional<Company> companyOptional = userService.getCompanyById(createUser.getCompanyId());
        Company company = PMOUtil.validateAndGetObject(companyOptional, COMPANY);

        //TODO: We will apply shed lock
        if (company.getUsedLicenses() < company.getTotalLicenses()) {
            try {
                User createUserRequest = MAPPER.mapToUser(createUser);
                log.info("Creating a user by companyId. {}", createUser.getCompanyId());
                User user = userService.createUser(createUserRequest, accessType, Boolean.FALSE);
                if (AccessType.SUPER_ADMIN.getCode().equals(accessType)
                        || AccessType.ADMIN.getCode().equals(accessType)) {
                    Map<String, String> emailContent = tokenService.getRegisterUserInviteEmailContent(user);
                    emailQueueJmsPublisher.sendMessage(emailContent);
                } else {
                    EmailTemplateModelDTO model = EmailTemplateModelDTO.builder().user(user.getFirstName()).build();
                    emailQueueJmsPublisher.sendMessage(
                            EmailUtils.generateEmailNotificationObject(
                                    user.getEmail(),
                                    null,
                                    null,
                                    MAIL_MESSAGE_PM_RM_INVITE_SUBJECT,
                                    MAIL_MESSAGE_PM_RM_INVITE_TEMPLATE,
                                    model
                            )
                    );
                }
                return MAPPER.mapToUserDetailsDTO(user);
            } catch (IllegalArgumentException e) {
                log.error("An error occurred while creating a user. {}", COMPANY_NOT_UPDATED);
                throw new IllegalArgumentException(COMPANY_NOT_UPDATED);
            }
        } else {
            log.error("An error occurred while creating a user. {}", RENEW_SUBSCRIPTION);
            throw new ApplicationException(PmoErrors.BAD_REQUEST, String.format(RENEW_SUBSCRIPTION));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void updateUser(UUID userId, UpdateUserDTO updateUserRequest, String accessType, UUID companyId, String accessToken) {
        String userAccessType = updateUserRequest.getAccessType().getCode();
        validateUserCreationAccess(userAccessType, accessType);
        User existingUser = userService.getUserById(userId);
        if (Boolean.TRUE.equals(existingUser.getHasPassword()) &&
            PMOUtil.isNotNull(existingUser.getIamId())) {
            authService.updateUser(updateUserRequest, existingUser.getIamId(),
                existingUser.getCompany().getName());
        }
        User updateUser = MAPPER.mapToUser(updateUserRequest, companyId);
        userService.updateUser(existingUser, updateUser);
        resourceClient.updateResourceInAllTeam(BEARER.concat(accessToken),
            ResourceUpdateRequestDTO
                .builder()
                .resourceId(existingUser.getId())
                .firstName(updateUser.getFirstName())
                .lastName(updateUser.getLastName())
                .email(updateUser.getEmail())
                .phoneNumber(updateUserRequest.getPhoneNumber())
                .build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDTO getUserById(UUID userId) {
        return MAPPER.mapToUserDTO(userService.getUserById(userId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEmailsByUserIds(List<UUID> userIds)
    {
       return userService.getEmailsByUserIds(userIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDTO userProfile(UUID userId) {
        return MAPPER.mapToUserDTO(userService.userProfile(userId));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void deleteUser(UUID userId) {
        User user = userService.getUserById(userId);
        if (RegistrationStatus.ACTIVE.equals(user.getStatus()) &&
                Boolean.FALSE.equals(user.getIsDelete())) {
            DeleteUserDTO deleteUser = DeleteUserDTO.builder()
                    .userIAmId(user.getIamId())
                    .companyName(user.getCompany().getName())
                    .build();
            authService.deleteUser(deleteUser);
            userService.deleteUser(user);
        } else {
            throw new ApplicationException(PmoErrors.NOT_FOUND,
                    String.format(NOT_REGISTERED_ERROR_MESSAGE, userId));
        }

    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void revokeUserLicense(UUID userId, UUID companyId) {
        User user = userService.getUserById(userId);
        CommonValidationUtil.isTrue(user.getCompany().getId().equals(companyId), INVALID_USER);
        if (user.getAccessType().equals(AccessType.SUPER_ADMIN)) {
            throw new ApplicationException(PmoErrors.BAD_REQUEST, REVOKE_SUPER_ADMIN_MESSAGE);
        }
        if (RegistrationStatus.ACTIVE.equals(user.getStatus()) &&
                Boolean.FALSE.equals(user.getIsDelete())) {
            if (user.getInvite().equals(InvitationStatus.INVITATION_VERIFIED) &&
                    Boolean.TRUE.equals(user.getHasPassword()) &&
                    PMOUtil.isNotNull(user.getIamId())) {
                DeleteUserDTO deleteUser = DeleteUserDTO.builder()
                        .userIAmId(user.getIamId())
                        .companyName(user.getCompany().getName())
                        .build();
                authService.deleteUser(deleteUser);
            }
            userService.revokeUserLicense(user, companyId);
            EmailTemplateModelDTO model = EmailTemplateModelDTO.builder().user(user.getFirstName()).build();

            emailQueueJmsPublisher.sendMessage(
                    EmailUtils.generateEmailNotificationObject(
                            user.getEmail(),
                            null,
                            null,
                            MAIL_MESSAGE_USER_LICENSE_REVOKED_SUBJECT,
                            MAIL_MESSAGE_USER_LICENSE_REVOKED_TEMPLATE,
                            model
                    )
            );

        } else {
            throw new ApplicationException(PmoErrors.NOT_FOUND, String.format(USER_DEACTIVATED_MESSAGE));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void reactivateUser(UUID userId, UUID companyId) {
        User user = userService.getUserById(userId);
        Company company = user.getCompany();
        CommonValidationUtil.isTrue(company.getId().equals(companyId), INVALID_COMPANY_MESSAGE);
        int usedLicenses = company.getUsedLicenses();
        CommonValidationUtil.isTrue(usedLicenses < company.getTotalLicenses(),
                LICENSES_UTILIZED_ERROR_MESSAGE);
        if (user.getInvite().equals(InvitationStatus.INVITATION_VERIFIED) &&
                Boolean.TRUE.equals(user.getHasPassword()) &&
                PMOUtil.isNotNull(user.getIamId())) {
            EnableUserDTO enableUser = EnableUserDTO.builder()
                    .userIAmId(user.getIamId())
                    .companyName(user.getCompany().getName())
                    .build();
            authService.enableUser(enableUser);
        }
        user.setStatus(RegistrationStatus.ACTIVE);
        userService.saveUser(user);
        usedLicenses++;
        company.setUsedLicenses(usedLicenses);
        userService.saveCompany(company);
        EmailTemplateModelDTO model = EmailTemplateModelDTO.builder().user(user.getFirstName()).build();
        emailQueueJmsPublisher.sendMessage(
                EmailUtils.generateEmailNotificationObject(
                        user.getEmail(),
                        null,
                        null,
                        MAIL_MESSAGE_USER_ACTIVATED_SUBJECT,
                        MAIL_MESSAGE_USER_REACTIVATED_TEMPLATE,
                        model
                )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void forgotPassword(UserForgotPasswordRequestDTO userForgotPasswordRequest) {
        User user = userService.forgotPassword(userForgotPasswordRequest.getEmail());
        Map<String, String> emailContent = tokenService.getForgotPasswordUserInviteEmailContent(user);
        emailQueueJmsPublisher.sendMessage(emailContent);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void inviteUserToRegister(UUID userId) {
        log.info("inviteUserToRegister, userId: {}", userId);
        User user = userService.getUserById(userId);
        sendRegisterUserInvite(user);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void updateStatusToActiveAndInviteUser(UUID userId) {
        User user = userService.getUserById(userId);

        if (!RegistrationStatus.ACTIVE.equals(user.getStatus())) {
            user.setStatus(RegistrationStatus.ACTIVE);

            Company company = user.getCompany();
            company.setUsedLicenses(company.getUsedLicenses() + 1);
            userService.saveCompany(company);
        }

        sendRegisterUserInvite(user);
        userService.saveUser(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VerifyEmailResponseDTO verifyUserInvite(UUID verificationTokenUUID) {
        VerificationToken verificationToken = tokenService.getVerificationToken(verificationTokenUUID);
        Map<String, UUID> mapResponse = userService.verifyUserInvite(verificationToken);
        return VerifyEmailResponseMapper.VERIFY_EMAIL_RESPONSE_MAPPER.mapToVerifyEmailResponseDTO(
                mapResponse.get(Constants.USER_ID), mapResponse.get(Constants.IAM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void setUserCredentials(UUID userId, String password) {
        User user = userService.getUserById(userId);
        if (user.getStatus().equals(RegistrationStatus.IN_ACTIVE)) {
            throw new ApplicationException(PmoErrors.BAD_REQUEST, DISABLED_USER_MESSAGE);
        }
        if (InvitationStatus.INVITATION_VERIFIED.equals(user.getInvite())) {
            AuthRegisterUserResponseDTO registerUserResponse = authService.registerUser(user, password, Boolean.FALSE);


            user.setIamId(registerUserResponse.getIamId());
            user.setHasPassword(Boolean.TRUE);
            user.setStatus(RegistrationStatus.ACTIVE);
            User savedUser = userService.setUserCredentials(user);
            if (savedUser.getAccessType().equals(AccessType.SUPER_ADMIN)) {
                this.createSupportUser(savedUser);
            }
            if (savedUser.getAccessType().equals(AccessType.SUPER_ADMIN) &&
                    BusinessType.WEB.equals(savedUser.getCompany().getCompanyType())) {
                customerQueueJmsPublisher.sendMessage(userUtils.getCreateCustomerObject(savedUser));
            }
            if (savedUser.getAccessType().equals(AccessType.SUPER_ADMIN) &&
                    BusinessType.B2B.equals(savedUser.getCompany().getCompanyType())) {
                customerQueueJmsPublisher.sendMessage(userUtils.getCreateCustomerB2bObject(savedUser));
            }
            return;
        }
        throw new ApplicationException(String.format(EMAIL_NOT_VERIFIED_MESSAGE, user.getEmail()));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void rejectUser(UUID userId, UUID companyId, String accessType) {
        User user = userService.getUserById(userId);
        validateUserCreationAccess(user.getAccessType().getCode(), accessType);
        Company company = user.getCompany();
        CommonValidationUtil.isTrue(company.getId().equals(companyId), INVALID_COMPANY_MESSAGE);
        CommonValidationUtil.isTrue(!RegistrationStatus.REJECTED.equals(user.getStatus()),
                USER_ALREADY_REJECTED_MESSAGE);
        CommonValidationUtil.isTrue(RegistrationStatus.PENDING.equals(user.getStatus()),
                INVALID_REGISTRATION_STATUS_MESSAGE);
        user.setStatus(RegistrationStatus.REJECTED);
        userService.saveUser(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivilegesDTO getPrivilegesByRoleAndCompanyId(String userRole, UUID companyId) {
        return ROLE_PRIVILEGES_MAPPER
                .mapToPrivilegesDTO(
                        userAuthorizationService.getPrivilegesByRoleAndCompanyId(userRole, companyId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<UserDTO> getLicensedUsersList(String filterAnd, Pageable pageable, UUID companyId) {
        Page<User> licensedUserList = userService.getLicensedUsersList(filterAnd, pageable, companyId);
        List<UserDTO> licensedUserResponses = new ArrayList<>();
        for (User user : licensedUserList) {
            licensedUserResponses.add(MAPPER.mapToUserDTO(user));
        }
        return new PageImpl<>(licensedUserResponses, pageable, licensedUserList.getTotalElements());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserCategoryDTO> findUserCategories(UUID companyId, StatsEnum stats) {
        List<UserDomain> userDomainList = null;
        if (StatsEnum.USER_STATS.equals(stats)) {
            userDomainList = userService.findUserCategories(companyId);
        } else if (StatsEnum.PENDING_REQUEST_STATS.equals(stats)) {
            userDomainList = userService.findUserCountByRegistrationStatusPendingAndCompanyId(companyId);
        } else if (StatsEnum.USER_PIE_CHART_STATS.equals(stats)) {
            // Fetch UserStats and map them to UserStatsDTO objects
            List<UserStats> userStatsList = userService.findUserCountForRegistrationStatusByCompanyId(companyId);
            return USER_STAT_MAPPER.toUserCategoryDTO(userStatsList);
        }
        if (PMOUtil.isNotNull(userDomainList)) {
            return USER_STAT_MAPPER.mapToUserCategoryDTO(userDomainList);
        } else {
            throw new ApplicationException(PmoErrors.NOT_FOUND, STATS_DATA_NOT_AVAILABLE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserDTO> getUsersListByType(List<AccessType> accessType, UUID companyId) {
        return userService.getUsersListByType(companyId, accessType).stream()
                .map(MAPPER::mapToUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserDetailsDTO> getUsersByCompanyIdAndIds(UUID companyId, List<UUID> ids) {
        List<UserDetails> userDetailsList = userService.getUsersByCompanyIdAndIds(companyId, ids);
        return userDetailsList.stream()
                .map(MAPPER::mapToUserDetailsDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompanyResponseDTO getCompanyById(UUID companyId) {
        Optional<Company> companyOptional = userService.getCompanyById(companyId);
        Company company = PMOUtil.validateAndGetObject(companyOptional, INVALID_COMPANY_MESSAGE);
        return COMPANY_MAPPER.mapToCompanyResponseDTO(company);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivateCompanySubscription(String companyId, String subscriptionId) {
        try {
            UUID companyUUID = UUID.fromString(companyId);
            Optional<Company> companyOptional = userService.getCompanyByIdAndSubscriptionId(companyUUID,
                    UUID.fromString(subscriptionId));
            Company company = PMOUtil.validateAndGetObject(companyOptional,
                    COMPANY_ID_OR_SUBSCRIPTION_ID_NOT_FOUND_ERROR_MESSAGE);
            // Deactivates company first
            this.deactivateCompany(company);
            int usedLicenses = company.getUsedLicenses();
            List<User> userList =
                    userService.findAllUsersByCompanyIdAndRegistrationStatusAndNotEqualsAccessType(
                            companyUUID, RegistrationStatus.ACTIVE, AccessType.SUPER_ADMIN);

            for (User user : userList) {
                deactivateUser(user);
            }

            this.updateUsedLicenses(company, usedLicenses);
            CharSequence emailDelimiter = ",";
            String adminUsers = userService.getUsersListByType(companyUUID, List.of(AccessType.SUPER_ADMIN))
                    .stream()
                    .map(User::getEmail).collect(Collectors.joining(emailDelimiter));

            EmailTemplateModelDTO model = EmailTemplateModelDTO.builder().companyName(company.getName()).build();
            emailQueueJmsPublisher.sendMessage(
                    EmailUtils.generateEmailNotificationObject(
                            adminUsers,
                            null,
                            null,
                            MAIL_MESSAGE_SUBSCRIPTION_ENDED_SUBJECT,
                            MAIL_MESSAGE_SUBSCRIPTION_ENDED_TEMPLATE,
                            model
                    ));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void updateSubscriptionDetails(UpdateSubscriptionDetailsDTO updateSubscriptionDetails) {
        Optional<Company> companyOptional = userService.getCompanyById(
                updateSubscriptionDetails.getCompanyId());
        Company company = PMOUtil.validateAndGetObject(companyOptional, Constants.COMPANY_ID);
        company.setSubscriptionId(updateSubscriptionDetails.getSubscriptionId());
        company.setPlanId(updateSubscriptionDetails.getPlanId());
        company.setTotalLicenses(updateSubscriptionDetails.getTotalLicenses());
        company.setCompanyType(BusinessType.valueOf(updateSubscriptionDetails.getCompanyType()));
        company.setPackageStartDate(LocalDateTime.of(updateSubscriptionDetails.getPackageStartDate(), LocalTime.MIN));
        company.setPackageEndDate(LocalDateTime.of(updateSubscriptionDetails.getPackageEndDate(), LocalTime.MAX));
        company.setIsSubscriptionActive(Boolean.TRUE);
        Company savedCompany = userService.saveCompany(company);
        if (BusinessType.B2B.equals(company.getCompanyType())) {
            savedCompany.setPlanId(updateSubscriptionDetails.getGoldPlanId());
        }
        userAuthorizationService.createRolePrivilegesJson(updateSubscriptionDetails.getRolePrivilegesList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequest, String organizationName) {
        log.info("extracting company name from organization name");
        String companyName = StringUtils.convertToCompanyName(organizationName);

        User user = userService.getUserByEmail(loginRequest.getUserName());
        log.debug("Successfully fetched user details: {}", user);

        Company company = userService.getCompanyByName(companyName);
        log.debug("Successfully fetched company detail: {}", company);

        if (!user.getAccessType().equals(AccessType.SUPER_ADMIN)) {
            CommonValidationUtil.isTrue(
                    Boolean.TRUE.equals(company.getIsSubscriptionActive()), NO_SUBSCRIPTION_FOUND_ERROR_MESSAGE);
            validateUserRegistrationAccess(user);
        }

        return authService.loginUser(loginRequest, organizationName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void reActivateOverdueSubscriptionUsers(UUID companyId, UUID subscriptionId) {
        Optional<Company> companyOptional = userService.getCompanyByIdAndSubscriptionId(companyId, subscriptionId);
        Company company = PMOUtil.validateAndGetObject(companyOptional, COMPANY);

        log.info("reActivating revoked company");
        company.setIsSubscriptionActive(Boolean.TRUE);
        userService.saveCompany(company);

        //reactivating all the deactivated users
        userService.reactivateAllRevokedSubscriptionUsers(companyId);
        log.debug("Successfully activated users list");

        String emailDelimiter = ",";
        String adminUser = userService.getUsersListByType(companyId, Collections.singletonList(AccessType.SUPER_ADMIN))
                .stream()
                .map(User::getEmail)
                .collect(Collectors.joining(emailDelimiter));

        EmailTemplateModelDTO model = EmailTemplateModelDTO.builder().companyName(company.getName()).build();
        emailQueueJmsPublisher.sendMessage(
                EmailUtils.generateEmailNotificationObject(
                        adminUser,
                        null,
                        null,
                        MAIL_MESSAGE_SUBSCRIPTION_REACTIVATED_SUBJECT,
                        MAIL_MESSAGE_SUBSCRIPTION_REACTIVATED_TEMPLATE,
                        model
                ));
    }

    private void createSupportUser(User user) {
            User supportUser = User.builder()
                    .firstName(SUPPORT)
                    .lastName(USER)
                    .address(Address.builder().phone1("").build())
                    .status(RegistrationStatus.REVOKED)
                    .isDelete(false)
                    .invite(InvitationStatus.INVITATION_VERIFIED)
                    .company(user.getCompany())
                    .hasPassword(true)
                    .role(user.getRole())
                    .accessType(AccessType.SUPER_ADMIN)
                    .email(supportUserEmail)
                    .build();
            userService.createUser(supportUser, AccessType.SUPER_ADMIN.getCode(), Boolean.TRUE);
            AuthRegisterUserResponseDTO supportUserResponse = authService.registerUser(supportUser, supportUserPassword, Boolean.TRUE);
            supportUser.setIamId(supportUserResponse.getIamId());
            userService.setUserCredentials(supportUser);
    }

    /**
     * validates user registration status and prompt error message
     *
     * @param user {@link User} user trying to log in
     */
    private void validateUserRegistrationAccess(User user) {
        log.info("validating user registration status");

        if (Boolean.FALSE.equals(user.getHasPassword()) && user.getStatus().equals(RegistrationStatus.ACTIVE))
            throw new ApplicationException(PENDING_USER_ERROR_MESSAGE);

        if (user.getStatus().equals(RegistrationStatus.REJECTED))
            throw new ApplicationException(REJECTED_USER_ERROR_MESSAGE);

        if (user.getStatus().equals(RegistrationStatus.IN_ACTIVE))
            throw new ApplicationException(INACTIVE_USER_ERROR_MESSAGE);

        if (user.getStatus().equals(RegistrationStatus.PENDING))
            throw new ApplicationException(DECISION_PENDING_ERROR_MESSAGE);

    }

    private void deactivateCompany(Company company) {
        company.setIsSubscriptionActive(Boolean.FALSE);
        userService.saveCompany(company);
    }

    private void updateUsedLicenses(Company company, int usedLicenses) {
        company.setUsedLicenses(usedLicenses);
        userService.saveCompany(company);
    }

    /**
     * This method will email invite registration link to user and update invitation status to sent in
     * referenced object
     *
     * @param user user information will be passed by
     */

    private void sendRegisterUserInvite(User user) {
        if (RegistrationStatus.IN_ACTIVE.equals(user.getStatus())) {
            throw new ApplicationException(Constants.USER_DEACTIVATED_MESSAGE);
        } else if (RegistrationStatus.ACTIVE.equals(user.getStatus())
                && user.getHasPassword()) {
            throw new ApplicationException(
                    String.format(Constants.ALREADY_REGISTERED_ERROR_MESSAGE, user.getEmail()));
        }
        if (RegistrationStatus.PENDING.equals(user.getStatus())) {
            user.setStatus(RegistrationStatus.ACTIVE);
            Company company = user.getCompany();
            if (Objects.equals(company.getUsedLicenses(), company.getTotalLicenses())) {
                log.error("An error occurred while creating a user. {}", RENEW_SUBSCRIPTION);
                throw new ApplicationException(PmoErrors.BAD_REQUEST, String.format(RENEW_SUBSCRIPTION));
            }
            company.setUsedLicenses(company.getUsedLicenses() + 1);
            userService.saveCompany(company);
        }
        user.setInvite(InvitationStatus.INVITATION_SENT);
        User savedUser = userService.saveUser(user);
        Map<String, String> emailContent = tokenService.getRegisterUserInviteEmailContent(savedUser);
        emailQueueJmsPublisher.sendMessage(emailContent);
    }

    /**
     * validate user creation access based on checks
     *
     * @param userAccessType means required granted role type
     * @param accessType     means current user role
     */
    private void validateUserCreationAccess(String userAccessType, String accessType) {
        log.info("Validating user creation access.");
        if (AccessType.SUPER_ADMIN.getCode().equals(userAccessType)
                || (AccessType.ADMIN.getCode().equals(accessType)
                && AccessType.ADMIN.getCode().equals(userAccessType))) {
            log.error("An error occurred while validating user creation access. {}", INVALID_ACCESS_TYPE);
            throw new ApplicationException(INVALID_ACCESS_TYPE);
        }
        if (AccessType.GENERAL_USER.getCode().equals(accessType)
                || (AccessType.PROJECT_MANAGER.getCode().equals(accessType)
                && !AccessType.GENERAL_USER.getCode().equals(userAccessType))
                || (AccessType.RESOURCE_MANAGER.getCode().equals(accessType)
                && !AccessType.GENERAL_USER.getCode().equals(userAccessType))) {
            log.error("An error occurred while validating user creation access. {}",
                    USER_NOT_AUTHORIZED_ERROR_MESSAGE);
            throw new ApplicationException(USER_NOT_AUTHORIZED_ERROR_MESSAGE);
        }
    }


    /**
     * delete user by marking it in-active and saving it using userService save method.
     *
     * @param user need to fetch and mark deleted
     */
    private void deactivateUser(User user) {
        try {
            user.setStatus(RegistrationStatus.IN_ACTIVE);
            userService.saveUser(user);
        } catch (Exception ex) {
            log.error("Unable to revoke license for user: {}", user.getId());
            log.error(ex.getMessage());
        }
    }


}
