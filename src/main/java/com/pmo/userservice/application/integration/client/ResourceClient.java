package com.pmo.userservice.application.integration.client;

import static com.pmo.userservice.infrastructure.utils.Constants.USER_UNABLE_LOGIN_ERROR_MESSAGE;

import com.pmo.common.dto.ApiResponseDTO;
import com.pmo.common.enums.PmoErrors;
import com.pmo.common.exception.ApplicationException;
import com.pmo.common.exception.FiegnClientException;
import com.pmo.userservice.application.integration.config.feign.ResourceFeignConfiguration;
import com.pmo.userservice.application.integration.dto.ResourceUpdateRequestDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "resource-client", url = "${resource.service.url}",
    configuration = ResourceFeignConfiguration.class)
public interface ResourceClient {

    /**
     * deletes a user from the resource service.
     *
     * @param resourceUpdateRequest {@link ResourceUpdateRequestDTO}
     */
    @PutMapping(path = "/resource-teams/resources",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @CircuitBreaker(name = "resource-client-circuitbreaker")
    @Retry(name = "resource-client", fallbackMethod = "updateResourceInTeamsFallback")
    ResponseEntity<ApiResponseDTO<Void>> updateResourceInAllTeam(
        @RequestHeader("Authorization") String authorization,
        @RequestBody ResourceUpdateRequestDTO resourceUpdateRequest);

    // Fallback method for 'updateResourceInTeamsFallback'
    default ResponseEntity<ApiResponseDTO<Void>> updateResourceInTeamsFallback(Exception e) {
        if (e instanceof FiegnClientException) {
            FiegnClientException feignClientException = (FiegnClientException) e;
            throw new ApplicationException(feignClientException.getCode(), feignClientException.getMessage());
        } else {
            // when connection is refused
            throw new ApplicationException(PmoErrors.BAD_REQUEST.getCode(), USER_UNABLE_LOGIN_ERROR_MESSAGE);
        }
    }

}
