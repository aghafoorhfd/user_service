package com.pmo.userservice.application.integration.client;


import com.pmo.common.dto.ApiResponseDTO;
import com.pmo.common.enums.PmoErrors;
import com.pmo.common.exception.ApplicationException;
import com.pmo.common.exception.ConflictException;
import com.pmo.common.exception.FiegnClientException;
import com.pmo.userservice.application.dto.LoginRequestDTO;
import com.pmo.userservice.application.dto.LoginResponseDTO;
import com.pmo.userservice.application.integration.config.feign.AuthFeignConfiguration;
import com.pmo.userservice.application.integration.dto.AuthDeleteUserDTO;
import com.pmo.userservice.application.integration.dto.AuthDeleteUserListDTO;
import com.pmo.userservice.application.integration.dto.AuthEnableUserDTO;
import com.pmo.userservice.application.integration.dto.AuthRegisterUserRequestDTO;
import com.pmo.userservice.application.integration.dto.AuthRegisterUserResponseDTO;
import com.pmo.userservice.application.integration.dto.AuthUpdateUserDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.UUID;

import static com.pmo.userservice.infrastructure.utils.Constants.USER_NOT_DELETED_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.USER_NOT_ENABLED_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.USER_NOT_UPDATED_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.USER_CREDENTIALS_NOT_SET_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.USER_LIST_NOT_DELETED_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.USER_UNABLE_LOGIN_ERROR_MESSAGE;

@FeignClient(name = "auth-client", url = "${auth.service.url}",
        configuration = AuthFeignConfiguration.class)
public interface AuthClient {

    /**
     * Register a users in auth service.
     *
     * @param registerUserRequest request body of registering a user
     * @return created user response
     */
    @PostMapping(path = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @CircuitBreaker(name = "auth-client-circuitbreaker")
    @Retry(name = "auth-client", fallbackMethod = "registerUserFallback")
    ResponseEntity<ApiResponseDTO<AuthRegisterUserResponseDTO>> registerUser(
            @RequestBody AuthRegisterUserRequestDTO registerUserRequest);

    // Fallback method for 'registerUser'
    default ResponseEntity<ApiResponseDTO<AuthRegisterUserResponseDTO>> registerUserFallback(Exception e) {
        if (e instanceof ApplicationException) {
            ApplicationException applicationException = (ApplicationException) e;
            throw new ApplicationException(applicationException.getCode(), applicationException.getMessage());
        } else if (e instanceof ConflictException) {
            ConflictException conflictException = (ConflictException) e;
            throw new ConflictException(conflictException.getCode(), conflictException.getMessage());
        } else {
            // when connection is refused
            throw new ApplicationException(PmoErrors.BAD_REQUEST.getCode(), USER_CREDENTIALS_NOT_SET_ERROR_MESSAGE);
        }
    }

    /**
     * deletes a user from the auth service.
     *
     * @param authDeleteUser {@link AuthDeleteUserDTO}
     */
    @DeleteMapping(path = "",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @CircuitBreaker(name = "auth-client-circuitbreaker")
    @Retry(name = "auth-client", fallbackMethod = "deleteUserFallback")
    ResponseEntity<ApiResponseDTO<Void>> deleteUser(
            @RequestBody AuthDeleteUserDTO authDeleteUser);

    // Fallback method for 'deleteUser'
    default ResponseEntity<ApiResponseDTO<Void>> deleteUserFallback(Exception e) {
        if (e instanceof FiegnClientException) {
            FiegnClientException feignClientException = (FiegnClientException) e;
            throw new ApplicationException(feignClientException.getCode(), feignClientException.getMessage());
        } else {
            // when connection is refused
            throw new ApplicationException(PmoErrors.BAD_REQUEST.getCode(), USER_NOT_DELETED_ERROR_MESSAGE);
        }
    }

    /**
     * enables a user in the auth service
     *
     * @param authEnableUser {@link AuthEnableUserDTO}
     */
    @PutMapping(path = "/enable",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @CircuitBreaker(name = "auth-client-circuitbreaker")
    @Retry(name = "auth-client", fallbackMethod = "enableUserFallback")
    ResponseEntity<ApiResponseDTO<Void>> enableUser(
            @RequestBody AuthEnableUserDTO authEnableUser);

    // Fallback method for 'enableUser'
    default ResponseEntity<ApiResponseDTO<Void>> enableUserFallback(Exception e) {
        if (e instanceof FiegnClientException) {
            FiegnClientException feignClientException = (FiegnClientException) e;
            throw new ApplicationException(feignClientException.getCode(), feignClientException.getMessage());
        } else {
            // when connection is refused
            throw new ApplicationException(PmoErrors.BAD_REQUEST.getCode(), USER_NOT_ENABLED_ERROR_MESSAGE);
        }
    }

    /**
     * deletes users from auth service
     *
     * @param authDeleteUserList list of users userIAmId
     * @return list of deleted users userIAmId
     */
    @DeleteMapping(path = "/list",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @CircuitBreaker(name = "auth-client-circuitbreaker")
    @Retry(name = "auth-client", fallbackMethod = "deleteUserListFallback")
    ResponseEntity<ApiResponseDTO<List<UUID>>> deleteUserList(
            @RequestBody AuthDeleteUserListDTO authDeleteUserList);

    // Fallback method for 'deleteUserList'
    default ResponseEntity<ApiResponseDTO<List<UUID>>> deleteUserListFallback(Exception e) {
        // when connection is refused
        throw new ApplicationException(PmoErrors.BAD_REQUEST.getCode(), USER_LIST_NOT_DELETED_ERROR_MESSAGE);
    }

    @PutMapping(path = "/update",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
    @CircuitBreaker(name = "auth-client-circuitbreaker")
    @Retry(name = "auth-client", fallbackMethod = "updateUserFallback")
    ResponseEntity<ApiResponseDTO<Void>> updateUser(
            @RequestBody AuthUpdateUserDTO authUpdateUser);

    // Fallback method for 'updateUser'
    default ResponseEntity<ApiResponseDTO<Void>> updateUserFallback(Exception e) {
        if (e instanceof FiegnClientException) {
            FiegnClientException feignClientException = (FiegnClientException) e;
            throw new ApplicationException(feignClientException.getCode(), feignClientException.getMessage());
        } else {
            // when connection is refused
            throw new ApplicationException(PmoErrors.BAD_REQUEST.getCode(), USER_NOT_UPDATED_ERROR_MESSAGE);
        }
    }

    @PostMapping(path = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @CircuitBreaker(name = "auth-client-circuitbreaker")
    @Retry(name = "auth-client", fallbackMethod = "loginUserFallback")
    ResponseEntity<ApiResponseDTO<LoginResponseDTO>> loginUser(
            @RequestBody LoginRequestDTO loginRequest, @RequestHeader("X-TENANT-ID") String organizationName);

    // Fallback method for 'loginUser'
    default ResponseEntity<ApiResponseDTO<LoginResponseDTO>> loginUserFallback(Exception exception) {
        if (exception instanceof ApplicationException) {
            ApplicationException applicationException = (ApplicationException) exception;
            throw new ApplicationException(applicationException.getCode(), applicationException.getMessage());
        } else if (exception instanceof FiegnClientException) {
            FiegnClientException feignClientException = (FiegnClientException) exception;
            throw new ApplicationException(feignClientException.getCode(), feignClientException.getMessage());
        } else {
            // when connection is refused
            throw new ApplicationException(PmoErrors.BAD_REQUEST.getCode(), USER_UNABLE_LOGIN_ERROR_MESSAGE);
        }
    }

}
