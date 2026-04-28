/*
 * Copyright 2026-present Scalabeusze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.scaledrop.sdiam.configuration.security;

import static com.scaledrop.sdiam.configuration.Constants.API_V1_PREFIX;
import static com.scaledrop.sdiam.configuration.security.role.ApiUserRole.DOCUMENTATION;
import static com.scaledrop.sdiam.configuration.security.role.ApiUserRole.INTERNAL;
import static org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrop.sdiam.configuration.exception.AccountServiceException;
import com.scaledrop.sdiam.configuration.security.properties.SecurityProperties;
import com.scaledrop.sdiam.configuration.security.properties.SecurityProperties.BasicAuthorization;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.NullSecurityContextRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final ObjectMapper objectMapper;

  @Configuration
  @RequiredArgsConstructor
  public static class AuthSecurityConfiguration {

    private static final String EXTRACTING_API_CREDENTIALS_ERROR_MESSAGE =
        "Error extracting API credentials";

    private static final String[] DOCS_PATHS = {
      "/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**"
    };
    private static final String[] ACTUATOR_PATHS = {"/actuator/**"};
    private final SecurityProperties securityProperties;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomBasicAuthenticationEntryPoint basicAuthenticationEntryPoint;
    private final SessionAuthenticationEntryPoint sessionAuthenticationEntryPoint;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;

    @Bean
    @Order(0)
    public SecurityFilterChain securityFilterChainActuator(HttpSecurity http) throws Exception {
      return http.securityMatcher(ACTUATOR_PATHS)
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .csrf(AbstractHttpConfigurer::disable) // NOSONAR
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChainDocs(HttpSecurity http) throws Exception {
      return http.securityMatcher(DOCS_PATHS)
          .authorizeHttpRequests(r -> r.requestMatchers(DOCS_PATHS).hasRole(DOCUMENTATION.name()))
          .httpBasic(
              customizer -> customizer.authenticationEntryPoint(basicAuthenticationEntryPoint))
          .csrf(AbstractHttpConfigurer::disable)
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .exceptionHandling(eh -> eh.accessDeniedHandler(customAccessDeniedHandler))
          .build();
    }

    @Bean
    @Order(2)
    protected SecurityFilterChain securityFilterChainJwtLogin(HttpSecurity http) throws Exception {
      http.securityMatcher(API_V1_PREFIX + "/session/**", "/oauth2/**", "/login/oauth2/**")
          .cors(AbstractHttpConfigurer::disable)
          .csrf(AbstractHttpConfigurer::disable)
          .requestCache(AbstractHttpConfigurer::disable)
          .securityContext(s -> s.securityContextRepository(new NullSecurityContextRepository()))
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .logout(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(
              r ->
                  r.requestMatchers(API_V1_PREFIX + "/session/login")
                      .permitAll()
                      .requestMatchers(API_V1_PREFIX + "/session/google")
                      .permitAll()
                      .requestMatchers("/oauth2/**", "/login/oauth2/**")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .exceptionHandling(
              eh ->
                  eh.authenticationEntryPoint(sessionAuthenticationEntryPoint)
                      .accessDeniedHandler(customAccessDeniedHandler));

      if (clientRegistrationRepository.getIfAvailable() != null) {
        http.oauth2Login(
            customizer ->
                customizer
                    .successHandler(oAuth2LoginSuccessHandler)
                    .failureHandler(oAuth2LoginFailureHandler));
      }

      return http.build();
    }

    @Bean
    @Order(3)
    protected SecurityFilterChain securityFilterChainEndpoints(HttpSecurity http) throws Exception {
      return http.cors(AbstractHttpConfigurer::disable)
          .csrf(AbstractHttpConfigurer::disable)
          .requestCache(AbstractHttpConfigurer::disable)
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .logout(AbstractHttpConfigurer::disable)
          .anonymous(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(
              r ->
                  r.requestMatchers(API_V1_PREFIX + "/accounts/**")
                      .hasAnyRole(INTERNAL.name())
                      .anyRequest()
                      .denyAll())
          .httpBasic(
              customizer -> customizer.authenticationEntryPoint(basicAuthenticationEntryPoint))
          .exceptionHandling(eh -> eh.accessDeniedHandler(customAccessDeniedHandler))
          .build();
    }

    @Autowired
    private void configureInMemoryAuthentication(AuthenticationManagerBuilder auth)
        throws Exception {
      BasicAuthorization documentation = securityProperties.getDocumentation();
      BasicAuthorization internal = securityProperties.getInternal();

      auth.inMemoryAuthentication()
          .withUser(getUsername(documentation))
          .password(getEncodedPassword(documentation))
          .roles(DOCUMENTATION.name())
          .and()
          .withUser(getUsername(internal))
          .password(getEncodedPassword(internal))
          .roles(INTERNAL.name());
    }

    private String getUsername(BasicAuthorization basicAuthorization) {
      return getCredential(basicAuthorization, BasicAuthorization::getUsername);
    }

    private String getEncodedPassword(BasicAuthorization basicAuthorization) {
      return createDelegatingPasswordEncoder()
          .encode(getCredential(basicAuthorization, BasicAuthorization::getPassword));
    }

    private String getCredential(
        BasicAuthorization basicAuthorization,
        Function<BasicAuthorization, String> credentialExtractor) {
      return Optional.ofNullable(basicAuthorization)
          .map(credentialExtractor)
          .orElseThrow(() -> new AccountServiceException(EXTRACTING_API_CREDENTIALS_ERROR_MESSAGE));
    }
  }

  @Bean
  public CustomAccessDeniedHandler customAccessDeniedHandler() {
    return new CustomAccessDeniedHandler(objectMapper);
  }
}
