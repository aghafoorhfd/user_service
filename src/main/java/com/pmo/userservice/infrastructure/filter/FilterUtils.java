package com.pmo.userservice.infrastructure.filter;

import com.pmo.common.enums.PmoErrors;
import com.pmo.common.exception.ApplicationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FilterUtils {
    public Object castToRequiredType(Class fieldType, String value) {
        if (fieldType.isAssignableFrom(Double.class)) {
            return Double.valueOf(value);
        } else if (fieldType.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(value);
        } else if (fieldType.isAssignableFrom(String.class)) {
            return String.valueOf(value);
        } else if (Enum.class.isAssignableFrom(fieldType)) {
            try {
                return Enum.valueOf(fieldType, value);
            } catch (Exception exc) {
                throw new ApplicationException(PmoErrors.BAD_REQUEST, value);
            }
        } else if (UUID.class.isAssignableFrom(fieldType)) {
            return UUID.fromString(value);
        }
        return null;
    }

    public Object castToRequiredType(Class fieldType, List<String> value) {
        List<Object> lists = new ArrayList<>();
        for (String s : value) {
            lists.add(castToRequiredType(fieldType, s));
        }
        return lists;
    }
}
