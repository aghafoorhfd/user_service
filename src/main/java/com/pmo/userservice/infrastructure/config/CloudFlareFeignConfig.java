package com.pmo.userservice.infrastructure.config;

import com.pmo.userservice.application.integration.config.feign.FeignConfiguration;
import feign.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Slf4j
public class CloudFlareFeignConfig extends FeignConfiguration {
    @Value("${cloudflare.service.readTimeout}")
    private int readTimeout;

    @Value("${cloudflare.service.connectTimeout}")
    private int connectTimeout;

    @Value("${cloudflare.service.followRedirects}")
    private Boolean followRedirects;

    @Bean
    public Request.Options authRequestOptions() {
        log.debug(format("CloudFlare API Read Timeout %s", readTimeout));
        log.debug(format("CloudFlare API Connect Timeout %s", connectTimeout));
        return new Request.Options(connectTimeout, TimeUnit.MILLISECONDS, readTimeout, TimeUnit.MILLISECONDS, followRedirects);
    }
}
