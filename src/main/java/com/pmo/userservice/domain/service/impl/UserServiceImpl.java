package com.pmo.userservice.domain.service.impl;

import com.pmo.common.enums.PmoErrors;
import com.pmo.common.exception.ApplicationException;
import com.pmo.common.exception.ConflictException;
import com.pmo.common.util.PMOUtil;
import com.pmo.userservice.domain.model.Address;
import com.pmo.userservice.domain.model.Company;
import com.pmo.userservice.domain.model.Role;
import com.pmo.userservice.domain.model.User;
import com.pmo.userservice.domain.model.VerificationToken;
import com.pmo.userservice.domain.repository.AddressRepository;
import com.pmo.userservice.domain.repository.CompanyRepository;
import com.pmo.userservice.domain.repository.RoleRepository;
import com.pmo.userservice.domain.repository.UserRepository;
import com.pmo.userservice.domain.repository.VerificationTokenRepository;
import com.pmo.userservice.domain.repository.projection.UserDetails;
import com.pmo.userservice.domain.repository.projection.UserDomain;
import com.pmo.userservice.domain.repository.projection.UserStats;
import com.pmo.userservice.domain.repository.specification.UserSpecification;
import com.pmo.userservice.domain.service.UserService;
import com.pmo.userservice.infrastructure.enums.AccessType;
import com.pmo.userservice.infrastructure.enums.BusinessType;
import com.pmo.userservice.infrastructure.enums.FilterOperationEnum;
import com.pmo.userservice.infrastructure.enums.InvitationStatus;
import com.pmo.userservice.infrastructure.enums.RegistrationStatus;
import com.pmo.userservice.infrastructure.filter.FilterBuilderService;
import com.pmo.userservice.infrastructure.filter.FilterCondition;
import com.pmo.userservice.infrastructure.filter.FilterUtils;
import com.pmo.userservice.infrastructure.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.pmo.common.util.PMOUtil.validateAndGetObject;
import static com.pmo.common.util.PMOUtil.validateOptionalIsEmpty;
import static com.pmo.userservice.infrastructure.mapper.CompanyMapper.COMPANY_MAPPER;
import static com.pmo.userservice.infrastructure.mapper.UserMapper.MAPPER;
import static com.pmo.userservice.infrastructure.utils.Constants.ALREADY_REGISTERED_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.COMPANY;
import static com.pmo.userservice.infrastructure.utils.Constants.DUPLICATE_PHONE_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.INCOMPLETE_REGISTRATION_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.INVALID_COMPANY_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.INVALID_TOKEN_CONTACT_ADMINISTRATOR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.INVALID_TOKEN_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.LICENSES_NOT_PRESENT;
import static com.pmo.userservice.infrastructure.utils.Constants.NOT_REGISTERED_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.ROLE;
import static com.pmo.userservice.infrastructure.utils.Constants.SUBDOMAIN;
import static com.pmo.userservice.infrastructure.utils.Constants.USER;
import static com.pmo.userservice.infrastructure.utils.Constants.USER_DEACTIVATED_MESSAGE;

/**
 * Service class for managing users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AddressRepository addressRepository;
    private final VerificationTokenRepository tokenRepository;
    private final RoleRepository roleRepository;
    private final FilterBuilderService filterBuilderService;
    private final FilterUtils filterUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    public User registerUser(User userRequest, UUID planPackageId, int requiredLicenses, String companyName) {
        log.info("Fetching a user in userRepository having email: {}", userRequest.getEmail());
        Optional<User> user = userRepository.findByEmail(userRequest.getEmail());

        log.info("Fetching company from companyRepository with company name: {}", companyName);
        Optional<Company> optionalCompany = companyRepository.findByName(companyName);

        if (user.isPresent()) {
            User userEntity = user.get();
            if (RegistrationStatus.ACTIVE.equals(userEntity.getStatus())) {
                PMOUtil.validateOptionalIsEmpty(optionalCompany, SUBDOMAIN);
            } else if (RegistrationStatus.IN_ACTIVE.equals(userEntity.getStatus()) ||
                    Boolean.TRUE.equals(userEntity.getIsDelete())) {
                throw new ConflictException(USER_DEACTIVATED_MESSAGE);
            }
            userEntity.setInvite(InvitationStatus.INVITATION_SENT);
            return userRepository.save(userEntity);
        }

        PMOUtil.validateOptionalIsEmpty(optionalCompany, SUBDOMAIN);
        log.info("Fetching a role for user in roleRepository having email: {}",
                userRequest.getEmail());
        Role role = PMOUtil.validateAndGetObject(roleRepository.findByName(
                String.valueOf(AccessType.SUPER_ADMIN)), ROLE);

        log.info("Saving an address for user in addressRepository having email: {}",
                userRequest.getEmail());
        Address address = addressRepository.save(Address.builder().build());

        log.info("Saving a company for user in companyRepository having email: {}",
                userRequest.getEmail());
        Company company = COMPANY_MAPPER.mapToCompany(userRequest, address, planPackageId, requiredLicenses,
                companyName, userRequest.getCompany().getName());
        if (company.getCompanyType() == BusinessType.WEB) {
            company.setId(UUID.randomUUID());
        }
        Company savedCompany = companyRepository.save(company);
        User newUser = MAPPER.mapToUser(userRequest, savedCompany);
        newUser.setAccessType(AccessType.SUPER_ADMIN);
        newUser.setStatus(RegistrationStatus.PENDING);
        newUser.setInvite(InvitationStatus.INVITATION_SENT);
        newUser.setRole(role);
        log.info("Creating a user in userRepository having email: {}", userRequest.getEmail());
        User userCreated = userRepository.save(newUser);
        log.info("Successfully registered a user in userRepository having userId: {}",
                userCreated.getId());

        return userCreated;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public User createUser(User createUser, String accessType, boolean supportUser) {
        validateOptionalIsEmpty(userRepository.findByEmail(createUser.getEmail()),
                String.format(ALREADY_REGISTERED_ERROR_MESSAGE, createUser.getEmail()));
        Optional<Company> companyOptional = companyRepository.findById(createUser.getCompany().getId());
        Company company = validateAndGetObject(companyOptional, INVALID_COMPANY_MESSAGE);
        // validate phone if it is already present
        if (Objects.nonNull(createUser.getAddress().getPhone1())) {
            validateOptionalIsEmpty(
                    userRepository.findFirstByCompanyIdAndAddressPhone1(createUser.getCompany().getId(),
                            createUser.getAddress().getPhone1()), String.format(DUPLICATE_PHONE_ERROR_MESSAGE,
                            createUser.getAddress().getPhone1()));
        }
        Address address = addressRepository.save(
                Address.builder().phone1(createUser.getAddress().getPhone1()).build());
        Role role = PMOUtil.validateAndGetObject(roleRepository.findByName(
                createUser.getAccessType().getCode()), ROLE);
        createUser.setAddress(address);
        createUser.setCompany(company);
        createUser.setRole(role);
        if (!supportUser && (AccessType.SUPER_ADMIN.getCode().equals(accessType)
                || AccessType.ADMIN.getCode().equals(accessType))) {
            //users added by admin or super admin will be by default active and invitation will be sent
            createUser.setStatus(RegistrationStatus.ACTIVE);
            createUser.setInvite(InvitationStatus.INVITATION_SENT);

            // update company used license by +1
            company.setUsedLicenses(company.getUsedLicenses() + 1);
            log.info("company id: {}, usedLicenses: {}", company.getId(), company.getUsedLicenses());

            Company savedCompany = companyRepository.save(company);
            log.debug("saved company: {}", savedCompany);

            createUser.setCompany(savedCompany);

            User savedUser = userRepository.save(createUser);
            log.debug("created user: {}", savedUser);
            log.info("created user id: {}, created email: {}", savedUser.getId(), savedUser.getEmail());
            return savedUser;
        }
        return userRepository.save(createUser);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void updateUser(User existingUser, User updateUser) {
        existingUser.setFirstName(updateUser.getFirstName());
        existingUser.setLastName(updateUser.getLastName());
        existingUser.setAccessType(updateUser.getAccessType());
        Role role = validateAndGetObject(roleRepository.findByName(
                updateUser.getAccessType().getCode()), ROLE);
        existingUser.setRole(role);
        Address address = existingUser.getAddress();
        // validate phone if it is already present
        validatePhone(updateUser.getCompany().getId(), address.getPhone1(),
                updateUser.getAddress().getPhone1());
        address.setPhone1(updateUser.getAddress().getPhone1());
        Address savedAddress = addressRepository.save(address);
        log.info("Updated user address details, id: {}, phone number: {}", savedAddress.getId(),
                savedAddress.getPhone1());
        existingUser = userRepository.save(existingUser);
        log.info("Updated user details, id: {}, firstName: {}, lastName: {}, accessType: {}",
                existingUser.getId(), existingUser.getFirstName(), existingUser.getLastName(), existingUser.getAccessType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserById(UUID userId) {
        return validateAndGetObject(userRepository.findById(userId),
                String.format(NOT_REGISTERED_ERROR_MESSAGE,
                        userId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User userProfile(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(PmoErrors.BAD_REQUEST));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(User user) {
        user.setIsDelete(true);
        user.setStatus(RegistrationStatus.IN_ACTIVE);
        userRepository.save(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revokeUserLicense(User user, UUID companyId) {
        user.setStatus(RegistrationStatus.REVOKED);
        userRepository.save(user);
        Optional<Company> companyOptional = companyRepository.findById(companyId);
        Company company = validateAndGetObject(companyOptional, INVALID_COMPANY_MESSAGE);
        if (company.getUsedLicenses() != null) {
            int licenses = company.getUsedLicenses();
            licenses--;
            company.setUsedLicenses(licenses);
            companyRepository.save(company);
        } else {
            throw new ApplicationException(PmoErrors.PMO_ERRORS, String.format(LICENSES_NOT_PRESENT));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User forgotPassword(String userEmail) {

        User user = validateAndGetObject(userRepository.findByEmail(userEmail), USER);
        if (RegistrationStatus.ACTIVE.equals(user.getStatus())) {
            user.setInvite(InvitationStatus.RESEND_INVITE);
            return userRepository.save(user);
        } else if (RegistrationStatus.PENDING.equals(user.getStatus())) {
            throw new ApplicationException(INCOMPLETE_REGISTRATION_ERROR_MESSAGE);
        }
        throw new ApplicationException(USER_DEACTIVATED_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User setUserCredentials(User user) {
//        user.setStatus(RegistrationStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        // delete user token, so it will not be accessible to use for next time
        tokenRepository.deleteByUserId(user.getId());
        return savedUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, UUID> verifyUserInvite(VerificationToken verificationToken) {
        Map<String, UUID> mapResponse = new HashMap<>();
        UUID userId = verificationToken.getUser().getId();
        if (LocalDateTime.now().isBefore(verificationToken.getExpiresAt())) {
            UUID iamId = setUserInvitationStatus(userId, InvitationStatus.INVITATION_VERIFIED);
            mapResponse.put(Constants.USER_ID, userId);
            mapResponse.put(Constants.IAM_ID, iamId);
            return mapResponse;
        }
        setUserInvitationStatus(userId, InvitationStatus.INVITATION_EXPIRED);
        if (AccessType.SUPER_ADMIN.equals(verificationToken.getUser().getAccessType())) {
            throw new ApplicationException(INVALID_TOKEN_MESSAGE);
        } else {
            throw new ApplicationException(INVALID_TOKEN_CONTACT_ADMINISTRATOR_MESSAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<User> getLicensedUsersList(String filterAnd, Pageable pageable, UUID companyId) {
        // todo  will send accessType and status once it is finalized will billing module
        List<FilterCondition> andConditions = filterBuilderService
                .createFilterCondition(filterAnd, FilterOperationEnum.EQUAL);

        FilterCondition companyFilter = FilterCondition.builder()
                .field(COMPANY)
                .operator(FilterOperationEnum.EQUAL)
                .value(companyId.toString())
                .build();
        andConditions.add(companyFilter);
        return userRepository.findAll(new UserSpecification(andConditions, filterUtils), pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserDomain> findUserCategories(UUID companyId) {
        return userRepository.findUserCategories(companyId.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getUsersListByType(UUID companyId, List<AccessType> accessType) {
        List<User> userList = userRepository.findAllByCompanyIdAndAccessTypeIn(companyId, accessType);
        if (PMOUtil.isEmpty(userList)) {
            log.info("Users not found against companyId: {}", companyId);
            return userList;
        }
        log.info("Successfully fetched User List By Type, User List Size: {}", userList.size());
        return userList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserDetails> getUsersByCompanyIdAndIds(UUID companyId, List<UUID> ids) {
        List<UserDetails> userDetailsList = userRepository.findAllByCompanyIdAndIdIn(companyId, ids);
        if (PMOUtil.isEmpty(userDetailsList)) {
            log.info("User details list not found against companyId: {}", companyId);
            return userDetailsList;
        }
        log.info("Successfully fetched User details List, User List Size: {}", userDetailsList.size());
        return userDetailsList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Company> getCompanyById(UUID companyId) {
        return companyRepository.findById(companyId);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Company> getCompanyByIdAndSubscriptionId(UUID companyId, UUID subscriptionId) {
        return companyRepository.findByIdAndSubscriptionId(companyId, subscriptionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> findAllUsersByCompanyIdAndRegistrationStatusAndNotEqualsAccessType(
            UUID companyId,
            RegistrationStatus registrationStatus, AccessType accessType) {
        return userRepository.findAllByCompanyIdAndStatusAndAccessTypeNot(companyId, registrationStatus,
                accessType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Company saveCompany(Company company) {
        Company savedCompany = companyRepository.save(company);
        log.info("saved company details, id: {}, name: {}", savedCompany.getId(),
                savedCompany.getName());
        log.debug("saved company details: {}", company);
        return savedCompany;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserDomain> findUserCountByRegistrationStatusPendingAndCompanyId(UUID companyId) {
        return userRepository.findUserCountByRegistrationStatusPendingAndCompanyId(companyId.toString());
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserStats> findUserCountForRegistrationStatusByCompanyId(UUID companyId) {
        return userRepository.findUserCountForRegistrationStatusByCompanyId(companyId.toString());
    }
    /**
     * validate the duplicate phone number of user
     *
     * @param previousPhoneNumber previousPhoneNumber contains phoneNumber which is already assigned
     *                            to a user
     * @param phoneNumber         updated phoneNumber received as param
     */
    private void validatePhone(UUID companyId, String previousPhoneNumber, String phoneNumber) {
        Optional<User> user = userRepository.findFirstByCompanyIdAndAddressPhone1(
                companyId, phoneNumber);
        if (Objects.nonNull(phoneNumber) && !Objects.equals(previousPhoneNumber, phoneNumber)
                && user.isPresent()
                && Objects.nonNull(user.get().getAddress())
                && Objects.equals(user.get().getAddress().getPhone1(), phoneNumber)) {
            throw new ConflictException(PmoErrors.ALREADY_EXISTS,
                    String.format(DUPLICATE_PHONE_ERROR_MESSAGE,
                            phoneNumber));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByEmail(String userEmail) {
        log.info("fetching user details by email");
        return validateAndGetObject(userRepository.findByEmail(userEmail), USER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Company getCompanyByName(String companyName) {
        log.info("fetching user company by name");
        return validateAndGetObject(companyRepository.findByName(companyName), COMPANY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reactivateAllRevokedSubscriptionUsers(UUID companyId) {
        log.info("fetching all the inactive users against company");
        List<User> deactivatedUserList = findAllUsersByCompanyIdAndRegistrationStatusAndNotEqualsAccessType(companyId,
                RegistrationStatus.IN_ACTIVE, AccessType.SUPER_ADMIN);
        deactivatedUserList.forEach(user -> user.setStatus(RegistrationStatus.ACTIVE));
        userRepository.saveAll(deactivatedUserList);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEmailsByUserIds(List<UUID> userIds) {
        return userRepository.findEmailsByIdIn(userIds);
    }

    /**
     * Sets user invitation status
     *
     * @param userId           unique identifier for user
     * @param invitationStatus status which needs to be set
     * @return UUID iam of the user
     */
    private UUID setUserInvitationStatus(UUID userId, InvitationStatus invitationStatus) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User userEntity = user.get();
            userEntity.setInvite(invitationStatus);
            userRepository.save(userEntity);
            return userEntity.getIamId();
        }
        throw new ApplicationException(PmoErrors.NOT_FOUND,
                String.format(NOT_REGISTERED_ERROR_MESSAGE, userId));
    }

}
