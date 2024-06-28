package com.pmo.userservice.application.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UserForgotPasswordRequestDTO {

  @Email(message = "please provide a valid email")
  @NotBlank(message = "Field should not be empty")
  private String email;
}
