package com.pmo.userservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RealmInfoDTO {

    private String companyName;
    private String clientSecret;
    private String publicKey;

}
