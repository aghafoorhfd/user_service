package com.pmo.userservice.application.controller;

import com.pmo.common.dto.ApiResponseDTO;
import com.pmo.common.dto.TokenClaimsDTO;
import com.pmo.userservice.application.dto.CompanyResponseDTO;
import com.pmo.userservice.application.dto.CreateUserDTO;
import com.pmo.userservice.application.dto.LoginRequestDTO;
import com.pmo.userservice.application.dto.LoginResponseDTO;
import com.pmo.userservice.application.dto.PrivilegesDTO;
import com.pmo.userservice.application.dto.UpdateUserDTO;
import com.pmo.userservice.application.dto.UserCategoryDTO;
import com.pmo.userservice.application.dto.UserCredentialsRequestDTO;
import com.pmo.userservice.application.dto.UserDTO;
import com.pmo.userservice.application.dto.UserDetailsDTO;
import com.pmo.userservice.application.dto.UserForgotPasswordRequestDTO;
import com.pmo.userservice.application.dto.UserRegisterRequestDTO;
import com.pmo.userservice.application.dto.VerifyEmailResponseDTO;
import com.pmo.userservice.application.service.ApplicationService;
import com.pmo.userservice.infrastructure.enums.AccessType;
import com.pmo.userservice.infrastructure.enums.BusinessType;
import com.pmo.userservice.infrastructure.enums.StatsEnum;
import com.pmo.userservice.infrastructure.utils.TokenUtils;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for managing users.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final ApplicationService applicationService;
    private final TokenUtils tokenUtils;

    /**
     * used to register a new company into system
     *
     * @param userRequest      {@link UserRegisterRequestDTO}
     * @param planPackageId    user's selected plan package
     * @param requiredLicenses user's selected no of licenses
     * @param companyName name of the company
     * @return ApiResponseDTO
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<Void>> registerUser(
            @Valid @RequestBody UserRegisterRequestDTO userRequest,
            @NotNull @RequestParam UUID planPackageId,
            @NotNull @RequestParam int requiredLicenses,
            @RequestHeader("X-TENANT-ID") String companyName) {

        log.info("Registering user having email: {}", userRequest.getEmail());
        log.debug("Register request payload: {}", userRequest);
        userRequest.setCompanyType(BusinessType.WEB.getCode());
        applicationService.registerUser(userRequest, planPackageId, requiredLicenses, companyName);
        log.info("Successfully registered user having email: {}", userRequest.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>());
    }

    /**
     * allows to-add user in company.
     *
     * @param createUser {@link CreateUserDTO}
     * @return UserDetailsDTO
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('RESOURCE_MANAGER', 'SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER' )")
    public ResponseEntity<ApiResponseDTO<UserDetailsDTO>> addUser(
            @Valid @RequestBody CreateUserDTO createUser) {
        log.info("Fetching token details to retrieve companyId and accessType for adding user.");
        TokenClaimsDTO tokenClaimsDTO = tokenUtils.getTokenClaims();
        createUser.setCompanyId(tokenClaimsDTO.getCompanyId());
        log.info("Adding user having email: {}", createUser.getEmail());
        log.debug("Add user request payload: {}", createUser);
        UserDetailsDTO userDetailsDTO = applicationService.addUser(createUser,
                tokenClaimsDTO.getAccessType());
        log.info("Successfully added user having userId: {}", userDetailsDTO.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(userDetailsDTO));
    }

    /**
     * allows to-update user info in company.
     *
     * @param userId     UUID of user
     * @param updateUser dto with user info
     * @return ApiResponseDTO
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('RESOURCE_MANAGER', 'SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER' )")
    public ResponseEntity<ApiResponseDTO<Void>> updateUser(@PathVariable UUID userId,
                                                           @Valid @RequestBody UpdateUserDTO updateUser) {
        log.info("Fetching token details to retrieve companyId and accessType for updating user.");
        TokenClaimsDTO tokenClaimsDTO = tokenUtils.getTokenClaims();
        log.info("Updating user having userId: {}", userId);
        log.debug("Update user request payload: {}", updateUser);
        applicationService.updateUser(userId, updateUser, tokenClaimsDTO.getAccessType(),
            tokenClaimsDTO.getCompanyId(), tokenUtils.getAccessToken());
        log.info("Successfully updated user having userId. {}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>());
    }

    /**
     * used to get information of user on basis of userID
     *
     * @param userId user id of user
     * @return UserDTO
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority( 'SUPER_ADMIN', 'ADMIN' )")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getUserById(@Valid @PathVariable UUID userId) {
        log.info("Fetching the user having userId: {}", userId);
        UserDTO response = applicationService.getUserById(userId);
        log.info("Successfully fetched the user having userId: {}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(response));
    }

    /**
     * return the object of login-ed user
     *
     * @return UserDTO
     */
    @GetMapping("/user-profile")
    public ResponseEntity<ApiResponseDTO<UserDTO>> userProfile() {
        log.info("Fetching token details to retrieve userId.");
        TokenClaimsDTO tokenClaimsDTO = tokenUtils.getTokenClaims();
        log.info("Fetching the user profile having userId: {}", tokenClaimsDTO.getUserId());
        UserDTO userDTO = applicationService.userProfile(tokenClaimsDTO.getUserId());
        log.info("Successfully fetched the user profile having userId: {}", userDTO.getId());
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(userDTO));
    }


    /**
     * used delete a user whose id is passed in param
     *
     * @param userId user id of user tobe deleted
     * @return ApiResponseDTO
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority( 'SUPER_ADMIN', 'ADMIN' )")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable UUID userId) {
        log.info("Deleting the user having userId: {}", userId);
        applicationService.deleteUser(userId);
        log.info("Successfully deleted the user having userId: {}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>());
    }

    /**
     * used to revoke the licence of a user
     *
     * @param userId User ID
     * @return ApiResponseDTO
     */
    @PutMapping("/{userId}/revoke")
    @PreAuthorize("hasAnyAuthority( 'SUPER_ADMIN', 'ADMIN' )")
    public ResponseEntity<ApiResponseDTO<Void>> revokeUserLicense(@PathVariable UUID userId) {
        log.info("Fetching token details");
        TokenClaimsDTO tokenClaimsDTO = tokenUtils.getTokenClaims();
        log.info("Revoking the user license having userId: {}", userId);
        applicationService.revokeUserLicense(userId, tokenClaimsDTO.getCompanyId());
        log.info("Successfully revoked the user license having userId: {}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>());
    }

    /**
     * used to reactivate a user.
     *
     * @param userId user ID
     * @return ApiResponseDTO
     */
    @PutMapping("/{userId}/reactivate")
    @PreAuthorize("hasAnyAuthority( 'SUPER_ADMIN', 'ADMIN' )")
    public ResponseEntity<ApiResponseDTO<Void>> reactivateUser(@PathVariable UUID userId) {
        log.info("Fetching token details to retrieve companyId.");
        TokenClaimsDTO tokenClaimsDTO = tokenUtils.getTokenClaims();
        log.info("Reactivating the user having userId: {}", userId);
        applicationService.reactivateUser(userId, tokenClaimsDTO.getCompanyId());
        log.info("Successfully reactivated the user having userId: {}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>());
    }

    /**
     * used to forgotPassword of a user
     *
     * @param userForgotPasswordRequest UserForgotPasswordRequestDTO
     * @return ApiResponseDTO
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDTO<Void>> forgotPassword(
            @Valid @RequestBody UserForgotPasswordRequestDTO userForgotPasswordRequest) {
        log.info("Forgot password request having email: {}", userForgotPasswordRequest.getEmail());
        applicationService.forgotPassword(userForgotPasswordRequest);
        log.info("Completed the forgot password request having email: {}",
                userForgotPasswordRequest.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>());
    }

    /**
     * allows to-send/resend invite to user.
     *
     * @param userId UUID of user
     * @return ApiResponseDTO
     */
    @PutMapping("/{userId}/invite")
    @PreAuthorize("hasAnyAuthority( 'SUPER_ADMIN', 'ADMIN' )")
    public ResponseEntity<ApiResponseDTO<Void>> inviteUser(@PathVariable UUID userId) {
        log.info("Inviting user having userId: {}", userId);
        applicationService.inviteUserToRegister(userId);
        log.info("Successfully invited the user having userId: {}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>());

    }

    /**
     * update user status to active and send user invite user email
     * This endpoint is not being used anywhere
     *
     * @param userId is required to mark active and fetch email to send email
     * @return void
     */

    @PutMapping("/{userId}/updateStatusToActiveAndInviteUser")
    public ResponseEntity<ApiResponseDTO<Void>> updateStatusToActiveAndInviteUser(
            @PathVariable UUID userId) {
        log.info("Updating the user status having userId: {}", userId);
        applicationService.updateStatusToActiveAndInviteUser(userId);
        log.info("Successfully updated the user status having userId: {}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>());
    }

    /**
     * used to verify a user email
     *
     * @param token UUID based token of user
     * @return VerifyEmailResponseDTO
     */
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponseDTO<VerifyEmailResponseDTO>> verifyEmail(
            @Valid @RequestParam UUID token) {
        log.info("Verifying the user email.");
        VerifyEmailResponseDTO response = applicationService.verifyUserInvite(token);
        log.info("Successfully verified the user email.");
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(response));
    }

    /**
     * used to set Password of newly created user
     *
     * @param credentialsRequest credentials request dto
     * @return ApiResponseDTO
     */
    @PostMapping("/credentials")
    public ResponseEntity<ApiResponseDTO<Void>> setUserCredentials(
            @Valid @RequestBody UserCredentialsRequestDTO credentialsRequest) {
        log.info("Setting user credentials having userId: {} ", credentialsRequest.getUserId());
        applicationService.setUserCredentials(credentialsRequest.getUserId(), credentialsRequest.getPassword());
        log.info("Successfully set user credentials having userId: {} ", credentialsRequest.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>());
    }

    /**
     * used to reject a user who is created by a PM|RM
     *
     * @param userId User ID
     * @return ApiResponseDTO
     */
    @PutMapping("/{userId}/reject")
    @PreAuthorize("hasAnyAuthority( 'SUPER_ADMIN', 'ADMIN' )")
    public ResponseEntity<ApiResponseDTO<Void>> rejectUser(@PathVariable UUID userId) {
        log.info("Fetching user token to retrieve companyId and accessType for rejecting user");
        TokenClaimsDTO tokenClaimsDTO = tokenUtils.getTokenClaims();
        log.info("Rejecting user having userId: {}", userId);
        applicationService.rejectUser(userId, tokenClaimsDTO.getCompanyId(),
                tokenClaimsDTO.getAccessType());
        log.info("Successfully rejected user having userId: {}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>());
    }

    /**
     * used to provide the privileges against the user role.
     *
     * @return PrivilegesResponseDTO
     */
    @GetMapping("/privileges")
    public ResponseEntity<ApiResponseDTO<PrivilegesDTO>> getPrivileges() {
        log.info("Fetching user token to retrieve companyId and accessType to get privileges.");
        TokenClaimsDTO tokenClaims = tokenUtils.getTokenClaims();
        log.info("Fetching user privileges having userId : {} ", tokenClaims.getUserId());
        PrivilegesDTO privilegesDTO = applicationService.getPrivilegesByRoleAndCompanyId(
                tokenClaims.getAccessType(),
                tokenClaims.getCompanyId());
        log.info("Successfully fetched user privileges having userId : {} ", tokenClaims.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(privilegesDTO));

    }

    /**
     * used to Get all licensed user of a company
     *
     * @param filterAnd  param to pass any filterAnd
     * @param pageNumber page Number
     * @param pageSize   page size
     * @return Page<UserDTO>
     */
    @GetMapping("/{pageNumber}/{pageSize}")
    @PreAuthorize("hasAnyAuthority( 'SUPER_ADMIN', 'ADMIN' )")
    public ResponseEntity<ApiResponseDTO<Page<UserDTO>>> getLicensedUsersList(
            @RequestParam(value = "filterAnd", required = false) String filterAnd,
            @PathVariable int pageNumber,
            @PathVariable int pageSize) {
        log.debug("Fetching licensed users list");
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
        TokenClaimsDTO tokenClaimsDTO = tokenUtils.getTokenClaims();
        Page<UserDTO> response = applicationService.getLicensedUsersList(filterAnd,
                pageable, tokenClaimsDTO.getCompanyId());
        log.debug("Successfully fetched licensed users list");
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(response));
    }

    /*
     *  will get Data Statistics API | User Categories of company
     *
     * @return List<UserCategoryDTO>
     */
    @GetMapping("/category/stats")
    @PreAuthorize("hasAnyAuthority( 'SUPER_ADMIN', 'ADMIN' )")
    public ResponseEntity<ApiResponseDTO<List<UserCategoryDTO>>> findUserCategories(
            @RequestParam(value = "stats", required = true) StatsEnum stats
    ) {
        log.info("Fetching user token");
        TokenClaimsDTO tokenClaimsDTO = tokenUtils.getTokenClaims();
        log.info("Fetching user categories");
        List<UserCategoryDTO> response = applicationService.findUserCategories(
                tokenClaimsDTO.getCompanyId(), stats);
        log.info("Successfully fetched user categories");
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(response));
    }

    /**
     * will get usersList based on company and access type
     *
     * @param accessType of user
     * @return List<UserDTO>
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority( 'SUPER_ADMIN', 'ADMIN', 'RESOURCE_MANAGER', 'PROJECT_MANAGER', 'GENERAL_USER' )")
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> getUsersListByType(
            @Valid @RequestParam List<AccessType> accessType) {
        log.info("Fetching user company to retrieve companyId to get users list");
        TokenClaimsDTO tokenClaimsDTO = tokenUtils.getTokenClaims();
        log.info("Fetching users list by type");
        List<UserDTO> response = applicationService.getUsersListByType(accessType,
                tokenClaimsDTO.getCompanyId());
        log.info("Successfully fetched users list by type");

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(response));
    }

    /**
     * used to get details of users on basis of userIDs
     * THis endpoint is not being used on UI
     *
     * @param userIds List of userIds
     * @return List<UserDetailsDTO>
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponseDTO<List<UserDetailsDTO>>> getUsersDetailsByUserIds(
            @Valid @RequestBody List<UUID> userIds) {
        log.info("Fetching user token to retrieve companyId to get user details.");
        TokenClaimsDTO tokenClaimsDTO = tokenUtils.getTokenClaims();
        log.info("Fetching user details");
        List<UserDetailsDTO> response = applicationService.getUsersByCompanyIdAndIds(
                tokenClaimsDTO.getCompanyId(), userIds);
        log.info("Successfully fetched user details");
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(response));
    }

    /**
     * used to get details of a company
     *
     * @return CompanyResponseDTO
     */
    @GetMapping("/company")
    @PreAuthorize("hasAnyAuthority( 'SUPER_ADMIN', 'ADMIN' )")
    public ResponseEntity<ApiResponseDTO<CompanyResponseDTO>> getCompanyDetails() {
        log.info("Fetching user token to retrieve companyId");
        TokenClaimsDTO tokenClaimsDTO = tokenUtils.getTokenClaims();
        CompanyResponseDTO companyResponse = applicationService.getCompanyById(
                tokenClaimsDTO.getCompanyId());
        log.info("Successfully fetched user company");
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(companyResponse));
    }

    /**
     * Logins user in an identity provider
     *
     * @param loginRequest {@link LoginRequestDTO}
     * @return {@link ResponseEntity<ApiResponseDTO<LoginResponseDTO>> }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> loginUser(
            @Valid @RequestBody LoginRequestDTO loginRequest,
            @RequestHeader("X-TENANT-ID") String organizationName) {
        log.info("Login user having user email: {}", loginRequest.getUserName());
        log.debug("Login Request payload: {}", loginRequest);
        LoginResponseDTO response = applicationService.loginUser(loginRequest, organizationName);
        log.info("User login successfully.");
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(response));
    }

}
