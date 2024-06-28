package com.pmo.userservice.application.integration.config.feign;

import feign.Logger;
import feign.okhttp.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {

  @Value("${feign.logging}")
  private String logging;

  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.valueOf(logging);
  }

  @Bean
  public OkHttpClient client() {
    return new OkHttpClient();
  }

}
