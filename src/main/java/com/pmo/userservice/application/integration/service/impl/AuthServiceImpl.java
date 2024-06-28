package com.pmo.userservice.application.integration.service.impl;

import com.pmo.common.dto.ApiResponseDTO;
import com.pmo.common.util.PMOUtil;
import com.pmo.userservice.application.dto.DeleteUserDTO;
import com.pmo.userservice.application.dto.DeleteUsersDTO;
import com.pmo.userservice.application.dto.EnableUserDTO;
import com.pmo.userservice.application.dto.LoginRequestDTO;
import com.pmo.userservice.application.dto.LoginResponseDTO;
import com.pmo.userservice.application.dto.UpdateUserDTO;
import com.pmo.userservice.application.integration.client.AuthClient;
import com.pmo.userservice.application.integration.dto.AuthRegisterUserRequestDTO;
import com.pmo.userservice.application.integration.dto.AuthRegisterUserResponseDTO;
import com.pmo.userservice.application.integration.service.AuthService;
import com.pmo.userservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.pmo.userservice.application.integration.mapper.AuthDeleteUserMapper.AUTH_DELETE_USER_MAPPER;
import static com.pmo.userservice.application.integration.mapper.AuthDeleteUsersMapper.AUTH_DELETE_USERS_MAPPER;
import static com.pmo.userservice.application.integration.mapper.AuthEnableUserMapper.AUTH_ENABLE_USER_MAPPER;
import static com.pmo.userservice.application.integration.mapper.AuthRegisterUserRequestMapper.AUTH_REGISTER_USER_REQUEST_MAPPER;
import static com.pmo.userservice.application.integration.mapper.AuthUpdateUserMapper.AUTH_UPDATE_USER_MAPPER;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthClient authClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthRegisterUserResponseDTO registerUser(User user, String password, boolean supportUser) {
        AuthRegisterUserRequestDTO authRegisterUserRequest
                = AUTH_REGISTER_USER_REQUEST_MAPPER.mapToAuthRegisterUserRequest(user, user.getCompany(), password, supportUser);

        final long callStartTime = System.currentTimeMillis();
        ResponseEntity<ApiResponseDTO<AuthRegisterUserResponseDTO>> responseEntity =
                authClient.registerUser(authRegisterUserRequest);
        ApiResponseDTO<AuthRegisterUserResponseDTO> responseBody = responseEntity.getBody();
        log.info("Auth service register user call took [{}] milliseconds",
                System.currentTimeMillis() - callStartTime);
        return Objects.requireNonNull(responseBody.getData());
    }

    @Override
    public void deleteUser(DeleteUserDTO deleteUser) {
        authClient.deleteUser(AUTH_DELETE_USER_MAPPER.mapToAuthDeleteUserDTO(deleteUser));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enableUser(EnableUserDTO enableUser) {
        authClient.enableUser(AUTH_ENABLE_USER_MAPPER.mapToAuthEnableUserDTO(enableUser));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UUID> disableUserList(DeleteUsersDTO deleteUsers) {
        if (PMOUtil.isNotNull(deleteUsers) && PMOUtil.isEmpty(deleteUsers.getUserIAmIdList())) {
            return Collections.emptyList();
        }
        ResponseEntity<ApiResponseDTO<List<UUID>>> responseEntity = authClient.deleteUserList(
                AUTH_DELETE_USERS_MAPPER.mapToAuthDeleteUsersDTO(deleteUsers));
        ApiResponseDTO<List<UUID>> responseBody = responseEntity.getBody();
        return Objects.requireNonNull(responseBody.getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(UpdateUserDTO updateUser, UUID userIAmId, String companyName) {
        authClient.updateUser(AUTH_UPDATE_USER_MAPPER.
                mapToAuthUpdateUser(updateUser, userIAmId, companyName));
    }

    /**
     * {@inheritDoc}
     */
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequest, String organizationName) {
        LoginResponseDTO loginResponse = null;
        log.info("making a feign request to auth service in order to validate and log in");
        ResponseEntity<ApiResponseDTO<LoginResponseDTO>> responseEntity = authClient.loginUser(loginRequest, organizationName);
        ApiResponseDTO<LoginResponseDTO> apiResponse = responseEntity.getBody();
        if (PMOUtil.isNotNull(apiResponse) && PMOUtil.isNotNull(apiResponse.getData())) {
            loginResponse = apiResponse.getData();
            log.debug("User successfully logged in through feign request {}", loginResponse);
        }
        return loginResponse;
    }


}
