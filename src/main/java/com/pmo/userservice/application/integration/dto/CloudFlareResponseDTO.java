package com.pmo.userservice.application.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CloudFlareResponseDTO {

    @JsonProperty(value = "result")
    private CreateSubdomainResponseDTO createSubdomainResponse;
    private boolean success;
}
