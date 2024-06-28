package com.pmo.userservice.application.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateSubdomainResponseDTO {
    private String id;
    @JsonProperty(value = "zone_id")
    private String zoneId;
    @JsonProperty(value = "zone_name")
    private String zoneName;
    private String name;
    private String type;
    private String content;
    private boolean proxiable;
    private boolean proxied;
    private int ttl;
    private boolean locked;
    private int priority;
}
