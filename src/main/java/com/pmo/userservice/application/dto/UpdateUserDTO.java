package com.pmo.userservice.application.dto;

import com.pmo.userservice.infrastructure.enums.AccessType;
import com.pmo.userservice.infrastructure.utils.Constants;
import io.micrometer.core.lang.Nullable;
import javax.validation.constraints.NotEmpty;
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
public class UpdateUserDTO {

  @NotEmpty
  @Pattern(regexp = Constants.NAME_REGEX, message = "please provide a valid first name")
  private String firstName;
  @NotEmpty
  @Pattern(regexp = Constants.NAME_REGEX, message = "please provide a valid last name")
  private String lastName;
  @NotNull(message = "Field should not be empty")
  private AccessType accessType;
  @Nullable
  @Pattern(regexp = Constants.PHONE_NUMBER_REGEX, message = "please provide a valid phone number")
  private String phoneNumber;
}
