package com.pmo.userservice.application.dto;

import com.pmo.userservice.infrastructure.enums.AccessType;
import com.pmo.userservice.infrastructure.enums.InvitationStatus;
import com.pmo.userservice.infrastructure.enums.RegistrationStatus;
import com.pmo.userservice.infrastructure.utils.Constants;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UserDTO {

  @NotNull(message = "Field should not be empty")
  private UUID id;
  @Pattern(regexp = Constants.NAME_REGEX, message = "please provide a valid name")
  private String firstName;
  @Pattern(regexp = Constants.NAME_REGEX, message = "please provide a valid name")
  private String lastName;
  @Pattern(regexp = Constants.LOGIN_REGEX, message = "please provide a valid email")
  private String email;
  @NotNull(message = "Field should not be empty")
  private String dob;
  @NotNull(message = "Field should not be empty")
  private String gender;
  @NotNull(message = "Field should not be empty")
  private String organizationName;
  @NotNull(message = "Field should not be empty")
  private String address;
  private String imageUrl;
  @NotNull(message = "Field should not be empty")
  private AccessType accessType;
  @NotNull(message = "Field should not be empty")
  private RegistrationStatus registrationStatus;
  @NotNull(message = "Field should not be empty")
  private InvitationStatus invitationStatus;
  private String phoneNumber;
  private String userName;
  private String accessTypeName;
  private UUID companyId;
  private Boolean hasPassword;

}
