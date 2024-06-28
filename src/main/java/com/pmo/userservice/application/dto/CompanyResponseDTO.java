package com.pmo.userservice.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pmo.userservice.infrastructure.enums.BusinessType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CompanyResponseDTO {

  private UUID id;
  private String name;
  private BusinessType companyType;
  private String description;
  private UUID subscriptionId;
  private Integer totalLicenses;
  private Integer usedLicenses;
  private LocalDateTime packageStartDate;
  private LocalDateTime packageEndDate;
}
