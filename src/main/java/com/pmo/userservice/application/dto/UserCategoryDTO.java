package com.pmo.userservice.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserCategoryDTO {

  private int noOfUsers;
  private String accessType;
  private String registrationStatus;

}
