package com.scaledrop.sdbff.adapter.client.upload.configuration;

import feign.Logger;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@EnableConfigurationProperties(UploadConfigProperties.class)
public class UploadClientConfiguration {

  private final UploadConfigProperties properties;

  @Bean
  RequestInterceptor uploadBasicAuthRequestInterceptor() {
    return new BasicAuthRequestInterceptor(properties.username(), properties.password());
  }

  @Bean
  ErrorDecoder uploadErrorDecoder() {
    return new UploadErrorDecoder();
  }

  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }
}
