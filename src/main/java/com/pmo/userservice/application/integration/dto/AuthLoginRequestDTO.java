package com.pmo.userservice.application.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class AuthLoginRequestDTO {

  private String userName;
  private String password;
  private String companyName;
  private String clientSecret;

}
