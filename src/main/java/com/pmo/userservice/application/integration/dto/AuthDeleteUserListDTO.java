package com.pmo.userservice.application.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AuthDeleteUserListDTO {

  private List<UUID> userIAmIdList;
  private String companyName;

}
