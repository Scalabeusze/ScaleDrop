package com.scaledrop.sdaccount.configuration;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ClockConfiguration {

  @Bean
  @Profile("!integration-test")
  public Clock clock() {
    return Clock.systemUTC();
  }

}
