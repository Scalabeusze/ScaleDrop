package com.scaledrop.sdbff.adapter.client.example.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@EnableConfigurationProperties(ExampleConfigProperties.class)
public class ExampleClientConfiguration {

  private final ExampleConfigProperties properties;

  @Bean
  RequestInterceptor basicAuthRequestInterceptor() {
    return new BasicAuthRequestInterceptor(properties.username(), properties.password());
  }

  @Bean
  ErrorDecoder exampleErrorDecoder(ObjectMapper objectMapper) {
    return new ExampleErrorDecoder(objectMapper);
  }
}
