package com.pmo.userservice.application.dto;

import com.pmo.userservice.domain.model.RolePrivileges;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UpdateSubscriptionDetailsDTO {

  private UUID companyId;
  private UUID subscriptionId;
  private UUID planId;
  private Integer totalLicenses;
  private String companyType;
  private LocalDate packageStartDate;
  private LocalDate packageEndDate;
  private String companyName;
  // This plan id is of gold/highest precedence planId and used to setting the role privileges of B2B company
  private UUID goldPlanId;
  private List<RolePrivileges> rolePrivilegesList;

}
