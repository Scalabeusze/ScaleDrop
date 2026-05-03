package com.scaledrop.sdbff.adapter.client.auth.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@EnableConfigurationProperties(AuthConfigProperties.class)
public class AuthClientConfiguration {

  private final AuthConfigProperties properties;

  @Bean
  public RequestInterceptor basicAuthRequestInterceptor() {
    return new BasicAuthRequestInterceptor(properties.username(), properties.password());
  }

  @Bean
  public ErrorDecoder authErrorDecoder(ObjectMapper objectMapper) {
    return new AuthErrorDecoder(objectMapper);
  }
}