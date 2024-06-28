package com.pmo.userservice.application.dto;

import com.pmo.userservice.infrastructure.utils.Constants;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ResendEmailVerificationRequestDTO {

    @NotEmpty
    @Pattern(regexp = Constants.NAME_REGEX, message = "Please provide a valid first name")
    private String firstName;
    @NotEmpty
    private String companyId;
    @NotEmpty
    @Pattern(regexp = Constants.NAME_REGEX, message = "Please provide a valid last name")
    private String lastName;
    @NotEmpty
    @Pattern(regexp = Constants.LOGIN_REGEX, message = "Please provide a valid email")
    private String email;
    @NotEmpty
    private String companyName;
}
