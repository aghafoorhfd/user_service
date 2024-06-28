package com.pmo.userservice.application.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class AuthEnableUserDTO {

  private UUID userIAmId;
  private String companyName;

}
