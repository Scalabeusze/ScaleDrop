package com.scaledrop.sdbff.adapter.client.iam.configuration;

import feign.Logger;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@EnableConfigurationProperties(IAMConfigProperties.class)
public class IAMClientConfiguration {

  private final IAMConfigProperties properties;

  @Bean
  RequestInterceptor downloadBasicAuthRequestInterceptor() {
    return new BasicAuthRequestInterceptor(properties.getUsername(), properties.getPassword());
  }

  @Bean
  public feign.codec.ErrorDecoder errorDecoder() {
    return new IAMErrorDecoder();
  }

  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }
}
