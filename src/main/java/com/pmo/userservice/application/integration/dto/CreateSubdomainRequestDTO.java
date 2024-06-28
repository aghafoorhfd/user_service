package com.pmo.userservice.application.integration.dto;

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
public class CreateSubdomainRequestDTO {
    private String type;
    private String name;
    private String content;
    private int ttl;
    private boolean proxied;
}
