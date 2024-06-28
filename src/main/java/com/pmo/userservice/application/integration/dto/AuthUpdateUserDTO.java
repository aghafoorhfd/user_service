package com.pmo.userservice.application.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUpdateUserDTO {

    private UUID userIAmId;
    private String firstName;
    private String lastName;
    private String companyName;
    private String accessType;

}
