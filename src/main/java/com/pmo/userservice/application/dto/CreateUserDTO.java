package com.pmo.userservice.application.dto;

import com.pmo.userservice.infrastructure.enums.AccessType;
import com.pmo.userservice.infrastructure.utils.Constants;
import io.micrometer.core.lang.Nullable;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateUserDTO {

  @NotEmpty
  @Pattern(regexp = Constants.NAME_REGEX, message = "Please provide a valid first name")
  private String firstName;
  @NotEmpty
  @Pattern(regexp = Constants.NAME_REGEX, message = "Please provide a valid last name")
  private String lastName;
  @NotEmpty
  @Pattern(regexp = Constants.LOGIN_REGEX, message = "Please provide a valid email")
  private String email;
  @NotNull(message = "AccessType should not be empty")
  private AccessType accessType;
  @Nullable
  @Pattern(regexp = Constants.PHONE_NUMBER_REGEX, message = "Please provide a valid phone number")
  private String phoneNumber;
  @Nullable
  private UUID companyId;
}
