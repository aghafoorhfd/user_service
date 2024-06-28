package com.pmo.userservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class LoginRequestDTO {

  @NotEmpty
  private String userName;
  @NotEmpty
  @ToString.Exclude
  private String password;
  private String companyName;
  @ToString.Exclude
  private String clientSecret;

}
