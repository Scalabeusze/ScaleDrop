package com.scaledrop.sdbff.adapter.client.iam.configuration;

import feign.Logger;
import org.springframework.context.annotation.Bean;

public class IAMClientConfiguration {

  @Bean
  public feign.codec.ErrorDecoder errorDecoder() {
    return new IAMErrorDecoder();
  }

  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }
}
