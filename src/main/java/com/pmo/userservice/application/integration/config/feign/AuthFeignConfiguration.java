package com.pmo.userservice.application.integration.config.feign;


import feign.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Slf4j
public class AuthFeignConfiguration extends FeignConfiguration {

  @Value("${auth.service.readTimeout}")
  private int readTimeout;

  @Value("${auth.service.connectTimeout}")
  private int connectTimeout;

  @Value("${auth.service.followRedirects}")
  private boolean followRedirects;

  /**
   * Sets request options of auth client
   *
   * @return bean of return option
   */
  @Bean
  public Request.Options authRequestOptions() {
    log.debug(format("Auth Service API Read Timeout %s", readTimeout));
    log.debug(format("Auth Service API Connect Timeout %s", connectTimeout));
    return new Request.Options(connectTimeout, TimeUnit.MILLISECONDS, readTimeout,
        TimeUnit.MILLISECONDS,
        followRedirects);
  }

}
