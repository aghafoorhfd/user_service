package com.pmo.userservice.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PrivilegesDTO {

  private List<PrivilegesDetailsDTO> privilegesDetails;
}
