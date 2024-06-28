package com.pmo.userservice.application.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceUpdateRequestDTO {
    @NotNull(message = "resourceId should not be null")
    private UUID resourceId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}
