package com.pmo.userservice.application.integration.client;

import com.pmo.userservice.application.integration.dto.CloudFlareResponseDTO;
import com.pmo.userservice.application.integration.dto.CreateSubdomainRequestDTO;
import com.pmo.userservice.infrastructure.config.CloudFlareFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import static com.pmo.userservice.infrastructure.utils.Constants.AUTHORIZATION;

@FeignClient(name = "CloudFlare", url = "${cloudflare.service.base-url}", configuration = CloudFlareFeignConfig.class)
public interface CloudFlareClient {

    /**
     * Get list of existing subdomains
     *
     * @param bearerToken bearer token
     * @return {@link CloudFlareResponseDTO }
     */
    @GetMapping(path = "${cloudflare.zone-id}" + "/dns_records", produces = MediaType.APPLICATION_JSON_VALUE)
    CloudFlareResponseDTO getZones(@RequestHeader(AUTHORIZATION) String bearerToken);

    /**
     * create a new subdomain
     *
     * @param bearerToken            bearer token
     * @param createSubdomainRequest request payload
     * @return {@link CloudFlareResponseDTO }
     */
    @PostMapping(path = "${cloudflare.zone-id}" + "/dns_records", produces = MediaType.APPLICATION_JSON_VALUE)
    CloudFlareResponseDTO createSubDomain(@RequestHeader(AUTHORIZATION) String bearerToken,
                                          @RequestBody CreateSubdomainRequestDTO createSubdomainRequest);
}
