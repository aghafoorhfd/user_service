package com.pmo.userservice.application.integration.config.feign;


import static java.lang.String.format;

import feign.Request;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Slf4j
public class ResourceFeignConfiguration extends FeignConfiguration {

    @Value("${resource.service.readTimeout}")
    private int readTimeout;

    @Value("${resource.service.connectTimeout}")
    private int connectTimeout;

    @Value("${resource.service.followRedirects}")
    private boolean followRedirects;

    /**
     * Sets request options of resource client
     *
     * @return bean of return option
     */
    @Bean
    public Request.Options resourceRequestOptions() {
        log.debug(format("Resource Service API Read Timeout %s", readTimeout));
        log.debug(format("Resource Service API Connect Timeout %s", connectTimeout));
        return new Request.Options(connectTimeout, TimeUnit.MILLISECONDS, readTimeout,
            TimeUnit.MILLISECONDS,
            followRedirects);
    }

}

