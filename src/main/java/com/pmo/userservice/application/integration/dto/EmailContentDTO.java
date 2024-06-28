package com.pmo.userservice.application.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class EmailContentDTO {

    private BigDecimal totalAmountPerCycle;
    private BigDecimal totalAmountTobeCharged;
    private int trialPeriod;
    private String currencyCode;
    private String currentCycleStartDate;
    private String currentCycleEndDate;

}
