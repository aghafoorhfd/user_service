package com.pmo.userservice.application.integration.service;

import com.pmo.userservice.application.dto.DeleteUserDTO;
import com.pmo.userservice.application.dto.DeleteUsersDTO;
import com.pmo.userservice.application.dto.EnableUserDTO;
import com.pmo.userservice.application.dto.LoginRequestDTO;
import com.pmo.userservice.application.dto.LoginResponseDTO;
import com.pmo.userservice.application.dto.UpdateUserDTO;
import com.pmo.userservice.application.integration.dto.AuthRegisterUserResponseDTO;
import com.pmo.userservice.domain.model.User;

import java.util.List;
import java.util.UUID;

public interface AuthService {

  /**
   * It registers a user in an auth service
   *
   * @param user     users info
   * @param password users password
   * @param supportUser
   * @return The response body of register user from auth service
   */
  AuthRegisterUserResponseDTO registerUser(User user, String password, boolean supportUser);

  /**
   * deletes a user from the auth service
   *
   * @param iAmId of a user
   */
  void deleteUser(DeleteUserDTO deleteUser);

  /**
   * Re-activates disabled user
   *
   * @param enableUserDTO to enable user
   */
  void enableUser(EnableUserDTO enableUserDTO);

  /**
   * disables a list of users
   *
   * @param deleteUsers delete Users request
   * @return list of disable users
   */
  List<UUID> disableUserList(DeleteUsersDTO deleteUsers);

  /**
   * Updates a user in the auth service
   *
   * @param updateUser {@link UpdateUserDTO} user's info to be updated
   * @param userIAmId id of the user in keycloak
   * @param companyName name of the company to which the user belongs to
   *
   */
  void updateUser(UpdateUserDTO updateUser, UUID userIAmId, String companyName);

  /**
   * makes a request to auth service in or order to validate user on keycloak and logs it in
   *
   * @param loginRequest     {@link LoginRequestDTO}
   * @param organizationName name of the company
   * @return {@link LoginResponseDTO}
   */
  LoginResponseDTO loginUser(LoginRequestDTO loginRequest, String organizationName);

}
