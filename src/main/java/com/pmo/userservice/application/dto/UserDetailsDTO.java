package com.pmo.userservice.application.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UserDetailsDTO {

  private UUID id;
  private String firstName;
  private String lastName;
  private String email;
  private String accessType;
  private String phoneNumber;
}
