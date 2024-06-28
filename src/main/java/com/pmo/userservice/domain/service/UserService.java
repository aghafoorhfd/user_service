package com.pmo.userservice.domain.service;

import com.pmo.userservice.domain.model.Company;
import com.pmo.userservice.domain.model.User;
import com.pmo.userservice.domain.model.VerificationToken;
import com.pmo.userservice.domain.repository.projection.UserDetails;
import com.pmo.userservice.domain.repository.projection.UserDomain;
import com.pmo.userservice.domain.repository.projection.UserStats;
import com.pmo.userservice.infrastructure.enums.AccessType;
import com.pmo.userservice.infrastructure.enums.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for managing users.
 */
public interface UserService {

    /**
     * Register a new user in to system and send user invite
     *
     * @param userRequest an object containing basic user info
     * @param planPackageId user's selected plan package
     * @param requiredLicenses user's required no of Licenses
     * @param organizationName name of the company
     * @return User
     */
    User registerUser(User userRequest, UUID planPackageId, int requiredLicenses, String companyName);

    /**
     * Create a new user in to system
     *
     * @param createUser {@link User}
     * @param accessType access type of resource trying to register user like super admin or admin and
     *                   so on
     * @param supportUser
     * @return void
     */
    User createUser(User createUser, String accessType, boolean supportUser);

    /**
     * Updates a user in to system
     *
     * @param existingUser {@link User} existing user entity
     * @param updateUser {@link User} containing user info that needs to be updated
     *
     */
    void updateUser(User existingUser, User updateUser);

    /**
     * Get user by id
     *
     * @param id persisted unique identifier for user
     * @return User
     */
    User getUserById(UUID id);

    /**
     * find in userRepository by id
     *
     * @param userId userId need to check in database
     * @return return User object to frontend
     */
    User userProfile(UUID userId);

    /**
     * Updates user registration status
     *
     * @param user user needs to be deleted
     */
    void deleteUser(User user);

    /**
     * inactive user and detect company licenses.
     *
     * @param user      set user inactive and set status
     * @param companyId used to fetch company by ID
     */
    void revokeUserLicense(User user, UUID companyId);

    /**
     * Sends a new link to user to reset password
     *
     * @param userEmail string containing email of user
     * @return User
     */
    User forgotPassword(String userEmail);


    /**
     * set userRepository to save credentials, and delete token so next time user will not use for set
     * using same link.
     *
     * @param user user will be used to set credentials
     * @return return user that has been saved
     */
    User setUserCredentials(User user);

    /**
     * Verifies user email invitation
     *
     * @param verificationToken system generated token for email verification
     * @return VerifyEmailResponse an object containing user id and iam id
     */
    Map<String, UUID> verifyUserInvite(VerificationToken verificationToken);

    /**
     * Get list of licensed users
     *
     * @param filterAnd search filter with & condition
     * @param pageable  size of page
     * @param companyId fetch used licenses of specific company identified by companyId
     * @return All users Page
     */
    Page<User> getLicensedUsersList(String filterAnd, Pageable pageable, UUID companyId);

    /**
     * get Data Statistics API | User Categories of current logged-in user's company
     *
     * @param companyId get userCategories by companyId
     * @return List
     */
    List<UserDomain> findUserCategories(UUID companyId);

    /**
     * get user list based of company ID and access type
     *
     * @param companyId  id of company
     * @param accessType of users
     * @return List
     */
    List<User> getUsersListByType(UUID companyId, List<AccessType> accessType);

    /**
     * get list of user details by companyId and users UUID's
     *
     * @param companyId UUID
     * @param ids       List<UUID>
     * @return List<UserDetails>
     */
    List<UserDetails> getUsersByCompanyIdAndIds(UUID companyId, List<UUID> ids);

    /**
     * fetch company using companyRepository by companyId
     *
     * @param companyId fetch company by I
     * @return return company that is optional, later need to check is present
     */
    Optional<Company> getCompanyById(UUID companyId);

    /**
     * get company by company id and subscription id
     *
     * @param companyId      to fetch company details
     * @param subscriptionId to fetch subscription details
     * @return {@link Optional<Company>}
     */
    Optional<Company> getCompanyByIdAndSubscriptionId(UUID companyId, UUID subscriptionId);

    /**
     * get all users by company id and status
     *
     * @param companyId          to fetch company details
     * @param registrationStatus registrations status of the user
     * @param accessType         to fetch access details
     * @return {@link Optional<Company>}
     */
    List<User> findAllUsersByCompanyIdAndRegistrationStatusAndNotEqualsAccessType(UUID companyId,
                                                                                  RegistrationStatus registrationStatus,
                                                                                  AccessType accessType);

    /**
     * saves a user
     *
     * @param user {@link User}
     * @return {@link User}
     */
    User saveUser(User user);

    /**
     * saves a company object and return saved DB object
     *
     * @param company Company to be saved
     * @return newly created company
     */
    Company saveCompany(Company company);
    /**
     * get Data Statistics API | User Categories of pending request user's company
     *
     * @param companyId get User Count by RegistrationStatus and companyId
     * @return List
     */
    List<UserDomain> findUserCountByRegistrationStatusPendingAndCompanyId(UUID companyId);
    /**
     * get Data Statistics API
     *
     * @param companyId get User Count of RegistrationStatus
     * @return List
     */
    List<UserStats> findUserCountForRegistrationStatusByCompanyId(UUID companyId);

    /**
     * fetches user based on email
     *
     * @param email email of the user
     * @return {@link User}
     */
    User getUserByEmail(String email);

    /**
     * fetches company based on organization name
     *
     * @param companyName name of the organization
     * @return {@link Company}
     */
    Company getCompanyByName(String companyName);

    /**
     * reactivates all users that were deactivated when subscription revoked
     *
     * @param companyId id of the company
     */
    void reactivateAllRevokedSubscriptionUsers(UUID companyId);

    /**
     * Retrieves a list of emails associated with the provided list of UUIDs.
     *
     * @param userIds The list of UUIDs for which to retrieve emails.
     * @return A list of emails associated with the provided UUIDs.
     */
    List<String> getEmailsByUserIds(List<UUID> userIds);
}
