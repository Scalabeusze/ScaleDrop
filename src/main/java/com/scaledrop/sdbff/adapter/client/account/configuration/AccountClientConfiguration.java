package com.scaledrop.sdbff.adapter.client.account.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@EnableConfigurationProperties(AccountConfigProperties.class)
public class AccountClientConfiguration {

  private final AccountConfigProperties properties;

  @Bean
  RequestInterceptor basicAuthRequestInterceptor() {
    return new BasicAuthRequestInterceptor(properties.username(), properties.password());
  }

  @Bean
  ErrorDecoder accountErrorDecoder(ObjectMapper objectMapper) {
    return new AccountErrorDecoder(objectMapper);
  }
}
