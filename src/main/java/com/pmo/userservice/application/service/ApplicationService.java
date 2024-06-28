package com.pmo.userservice.application.service;

import com.pmo.userservice.application.dto.CompanyResponseDTO;
import com.pmo.userservice.application.dto.CreateUserDTO;
import com.pmo.userservice.application.dto.LoginRequestDTO;
import com.pmo.userservice.application.dto.LoginResponseDTO;
import com.pmo.userservice.application.dto.PrivilegesDTO;
import com.pmo.userservice.application.dto.PrivilegesResponseDTO;
import com.pmo.userservice.application.dto.ResendEmailVerificationRequestDTO;
import com.pmo.userservice.application.dto.UpdateSubscriptionDetailsDTO;
import com.pmo.userservice.application.dto.UpdateUserDTO;
import com.pmo.userservice.application.dto.UserCategoryDTO;
import com.pmo.userservice.application.dto.UserDTO;
import com.pmo.userservice.application.dto.UserDetailsDTO;
import com.pmo.userservice.application.dto.UserForgotPasswordRequestDTO;
import com.pmo.userservice.application.dto.UserRegisterRequestDTO;
import com.pmo.userservice.application.dto.VerifyEmailResponseDTO;
import com.pmo.userservice.application.integration.dto.EmailContentDTO;
import com.pmo.userservice.infrastructure.enums.AccessType;
import com.pmo.userservice.infrastructure.enums.StatsEnum;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationService {

    /**
    * Register a new user in to system and send user invite
    *
    * @param userRegisterRequest an object containing basic user info
    * @param planPackageId user's selected plan package
    * @param requiredLicenses user's required no of licenses
    * @param companyName subdomain of company
    */
    void registerUser(UserRegisterRequestDTO userRegisterRequest, UUID planPackageId,
                      int requiredLicenses, String companyName);

    /**
     * This method register a new B2B company, with free trial subscription details and send invite
     *
     * @param userRegisterRequest       {@link UserRegisterRequestDTO} user request details
     * @param updateSubscriptionDetails {@link UpdateSubscriptionDetailsDTO} subscription details
     * @param emailContent {@link EmailContentDTO} Email content
     */
    void registerEnterpriseUser(UserRegisterRequestDTO userRegisterRequest, EmailContentDTO emailContent,
                                UpdateSubscriptionDetailsDTO updateSubscriptionDetails);

    /**
     * Generate verification url
     *
     * @param email
     * @return string verification Url
     */
    String generateResendInviteVerificationURL(String email);

    /**
     * Add new user under a company
     *
     * @param createUser {@link CreateUserDTO}
     * @param accessType dto having user  info
     * @return void
     */
    UserDetailsDTO addUser(CreateUserDTO createUser, String accessType);

    /**
     * Add new user under a company
     *
     * @param userId     UUID of user
     * @param updateUser {@link UpdateUserDTO} user's info that needs to be updated
     * @param accessType role of the user
     * @param companyId  id of the company to which the user belongs to
     * @param accessToken accessToken of user
     */
    void updateUser(UUID userId, UpdateUserDTO updateUser, String accessType, UUID companyId, String accessToken);

    /**
     * Get user by id
     *
     * @param id persisted unique identifier for user
     * @return UserDTO
     */
    UserDTO getUserById(UUID id);

    /**
     * Retrieves the email addresses of users based on their unique identifiers.
     *
     * @param userIds A list of UUIDs representing the unique identifiers of users.
     * @return A list of email addresses corresponding to the provided user IDs.
     */
    List<String> getEmailsByUserIds(List<UUID> userIds);

    /**
     * Get user profile by id
     *
     * @param userId persisted unique identifier for user
     * @return UserDTO
     */
    UserDTO userProfile(UUID userId);

    /**
     * Updates user registration status
     *
     * @param userId UUID id of user
     */
    void deleteUser(UUID userId);

    /**
     * Revoke User License and increase company remaining licenses.
     *
     * @param userId    -- to get user details
     * @param companyId -- to get company details
     */
    void revokeUserLicense(UUID userId, UUID companyId);

    /**
     * Re-activates revoked user
     *
     * @param userId    to get user details
     * @param companyId to get company details
     */
    void reactivateUser(UUID userId, UUID companyId);

    /**
     * Sends a new link to user to reset password
     *
     * @param userForgotPasswordRequest an object containing email of user
     */
    void forgotPassword(UserForgotPasswordRequestDTO userForgotPasswordRequest);

    /**
     * This endpoint allows to send/resend invite to user.
     *
     * @param userId UUID of user
     */
    void inviteUserToRegister(UUID userId);

    /**
     * when super admin or admin approve any resources added by other access type then we need to make
     * user status active and send invitation link
     *
     * @param userId used to get user details from user service
     */
    void updateStatusToActiveAndInviteUser(UUID userId);

    /**
     * Verifies user email invitation
     *
     * @param verificationToken system generated token for email verification
     * @return VerifyEmailResponseDTO an object containing user id and iam id
     */
    VerifyEmailResponseDTO verifyUserInvite(UUID verificationToken);

    /**
     * Sets user credentials and actives its  registration
     *
     * @param userId   persisted unique identifier for user
     * @param password user password to be set through auth
     */
    void setUserCredentials(UUID userId, String password);

    /**
     * rejects user
     *
     * @param userId     persisted id for user
     * @param companyId  persisted id for company
     * @param accessType access type of the user rejecting any user
     */
    void rejectUser(UUID userId, UUID companyId, String accessType);

    /**
     * Fetch the privileges against the user role.
     *
     * @param userRole  role of the user
     * @param companyId persisted unique identifier for company
     * @return {@link PrivilegesResponseDTO}
     */
    PrivilegesDTO getPrivilegesByRoleAndCompanyId(String userRole, UUID companyId);

    /**
     * gets list of licensed user
     *
     * @param filterAnd search filter with & condition
     * @param pageable  size of page
     * @param companyId to get company details
     * @return All users Page
     */
    Page<UserDTO> getLicensedUsersList(String filterAnd, Pageable pageable, UUID companyId);

    /**
     * get Data Statistics API | User Categories of current logged-in user's company
     *
     * @param companyId to get company details
     * @param stats to get stats
     * @return List
     */
    List<UserCategoryDTO> findUserCategories(UUID companyId, StatsEnum stats);

    /**
     * get users list by type -- give access type list(for example super admin and admin) and company
     * and get active users enrolled
     *
     * @param accessType role of user
     * @param companyId  to get company
     * @return list of active users
     */
    List<UserDTO> getUsersListByType(List<AccessType> accessType, UUID companyId);

    /**
     * gets list of user detail by companyId and user UUID's
     *
     * @param companyId UUID
     * @param ids       List<UUID>
     * @return list of user details
     */
    List<UserDetailsDTO> getUsersByCompanyIdAndIds(UUID companyId, List<UUID> ids);

    /**
     * Get company by id
     *
     * @param companyId UUID
     * @return users company info
     */
    CompanyResponseDTO getCompanyById(UUID companyId);


    /**
     * Deactivates company subscription
     *
     * @param companyId      to get company details
     * @param subscriptionId to get subscription details
     */
    void deactivateCompanySubscription(String companyId, String subscriptionId);


    /**
     * get company details from updateSubscriptionDetails.companyId, set required subscription details
     * and store it using user service
     *
     * @param updateSubscriptionDetails object to update company subscription details
     */
    void updateSubscriptionDetails(UpdateSubscriptionDetailsDTO updateSubscriptionDetails);

    /**
     * validates user credentials and subscription and logs it in.
     *
     * @param loginRequest     {@link LoginRequestDTO}
     * @param organizationName name of the company
     * @return {@link LoginResponseDTO}
     */
    LoginResponseDTO loginUser(LoginRequestDTO loginRequest, String organizationName);

    /**
     * reactivates all inactive users against overdue company
     *
     * @param companyId id of the company registered
     * @param subscriptionId subscription of the company
     */
    void reActivateOverdueSubscriptionUsers(UUID companyId, UUID subscriptionId);

}
