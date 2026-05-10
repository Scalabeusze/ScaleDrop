package com.scaledrop.sdupload.configuration;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
public class TimeProvider {

  private final Clock clock;

  public Instant calculateThreshold(Duration timeout) {
    return now().minus(timeout).toInstant();
  }

  public OffsetDateTime now() {
    return OffsetDateTime.now(clock);
  }
}
