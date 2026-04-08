package com.scaledrop.sddownload.utilities

import static java.time.LocalDateTime.of
import static java.time.ZoneId.systemDefault
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import java.time.Clock
import java.time.ZoneOffset

@Component
@Profile('integration-test')
@Primary
class ClockStub extends Clock {

  private Clock defaultClock

  @Delegate
  private Clock clock

  ClockStub() {
    defaultClock = fixed(of(2022, 10, 10, 15, 0).toInstant(ZoneOffset.UTC), systemDefault())
    clock = defaultClock
  }

  void reset() {
    clock = defaultClock
  }

  void realClock() {
    clock = systemDefaultZone()
  }
}
