package com.scaledrop.sdbff.utilities

import groovy.util.logging.Slf4j
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

@Slf4j
class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  static GenericContainer redis = new GenericContainer("redis:7-alpine")
      .withExposedPorts(6379)
      .waitingFor(
          Wait.forLogMessage(".*Ready to accept connections.*\\n", 1)
      )

  @Override
  void initialize(ConfigurableApplicationContext applicationContext) {
    redis.start()

    String host = redis.getHost()
    Integer port = redis.getMappedPort(6379)

    log.info("Started Redis container at {}:{}", host, port)

    TestPropertyValues.of(
        "spring.data.redis.host=$host",
        "spring.data.redis.port=$port",
        "spring.data.redis.password=",
        "spring.data.redis.ssl.enabled=false"
    ).applyTo(applicationContext.environment)
  }
}
