package com.pmo.userservice.application.dto;

import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UserCredentialsRequestDTO {

  @NotNull(message = "Should not be empty")
  UUID userId;

  @NotBlank(message = "Should not be blank")
  String password;
}
