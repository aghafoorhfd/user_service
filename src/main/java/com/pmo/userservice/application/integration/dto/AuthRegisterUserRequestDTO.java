package com.pmo.userservice.application.integration.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AuthRegisterUserRequestDTO {

  private String userName;
  private String password;
  private String firstName;
  private String lastName;
  private UUID companyId;
  private String companyName;
  private UUID userId;
  private String emailAddress;
  private String accessType;
  private boolean supportUser;
}
