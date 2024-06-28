package com.pmo.userservice.application.integration.config;


import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@Slf4j
public class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

  /**
   * configures keycloak instance
   *
   * @param http the {@link HttpSecurity} to modify
   * @throws Exception throws error when configuring Keycloak instance
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    log.info("Configuring keycloak HttpSecurity instance");
    super.configure(http);
    http.authorizeRequests()
        .anyRequest()
        .permitAll();
    http.csrf().disable();
    log.info("Successfully configured keycloak HttpSecurity instance");
  }

  /**
   * configures keycloak instance globally
   *
   * @param auth {@link AuthenticationManagerBuilder}
   */
  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    log.info("Configuring globally keycloak HttpSecurity instance");
    KeycloakAuthenticationProvider keycloakAuthenticationProvider =
        keycloakAuthenticationProvider();
    SimpleAuthorityMapper authorityMapper = new SimpleAuthorityMapper();
    authorityMapper.setPrefix("");
    keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(authorityMapper);
    auth.authenticationProvider(keycloakAuthenticationProvider);
    log.info("Successfully configured globally keycloak HttpSecurity instance");
  }

  /**
   * creates an instance of SessionAuthenticationStrategy
   *
   * @return {@link SessionAuthenticationStrategy}
   */
  @Bean
  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    log.info("Configuring keycloak SessionAuthenticationStrategy instance");
    return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
  }

}