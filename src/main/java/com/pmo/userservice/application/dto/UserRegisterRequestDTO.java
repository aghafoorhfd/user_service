package com.pmo.userservice.application.dto;

import com.pmo.userservice.infrastructure.utils.Constants;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserRegisterRequestDTO {

  @NotEmpty
  @Pattern(regexp = Constants.NAME_REGEX, message = "Please provide a valid first name")
  private String firstName;
  @NotEmpty
  @Pattern(regexp = Constants.NAME_REGEX, message = "Please provide a valid last name")
  private String lastName;
  @NotEmpty
  @Pattern(regexp = Constants.LOGIN_REGEX, message = "Please provide a valid email")
  private String email;
  private String companyType;
  private UUID companyId;
  @NotEmpty
  private String organizationName;
}
