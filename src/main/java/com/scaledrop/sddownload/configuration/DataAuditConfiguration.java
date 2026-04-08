package com.scaledrop.sddownload.configuration;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
@RequiredArgsConstructor
public class DataAuditConfiguration {

  private final Clock clock;

  @Bean
  @Primary
  public DateTimeProvider dateTimeProvider() {
    return () -> Optional.of(OffsetDateTime.now(clock));
  }
}
