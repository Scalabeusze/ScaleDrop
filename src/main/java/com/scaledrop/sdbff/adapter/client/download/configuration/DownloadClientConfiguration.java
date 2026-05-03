package com.scaledrop.sdbff.adapter.client.download.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Logger;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@EnableConfigurationProperties(DownloadConfigProperties.class)
public class DownloadClientConfiguration {

  private final DownloadConfigProperties properties;

  @Bean
  RequestInterceptor downloadBasicAuthRequestInterceptor() {
    return new BasicAuthRequestInterceptor(properties.username(), properties.password());
  }

  @Bean
  ErrorDecoder downloadErrorDecoder(ObjectMapper objectMapper) {
    return new DownloadErrorDecoder(objectMapper);
  }

  @Bean
  Logger.Level feignLoggerLevel() {
    return properties.url().contains("localhost") ? Logger.Level.FULL : Logger.Level.BASIC;
  }
}