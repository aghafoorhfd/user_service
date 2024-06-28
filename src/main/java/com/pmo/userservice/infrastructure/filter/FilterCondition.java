package com.pmo.userservice.infrastructure.filter;

import com.pmo.userservice.infrastructure.enums.FilterOperationEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class FilterCondition {
    private String field;
    private FilterOperationEnum operator;
    private Object value;
}
