package com.pmo.userservice.infrastructure.annotations;

import com.pmo.userservice.infrastructure.enums.AccessType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class RoleValidator implements ConstraintValidator<RoleValidation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return AccessType.stream().anyMatch(accessType -> Objects.equals(accessType.getCode(), value));
    }
}
