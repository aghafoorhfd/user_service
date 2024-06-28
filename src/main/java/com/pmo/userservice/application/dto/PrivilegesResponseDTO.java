package com.pmo.userservice.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PrivilegesResponseDTO {

  private Map<String, PrivilegesDetailsDTO> pages;
}
