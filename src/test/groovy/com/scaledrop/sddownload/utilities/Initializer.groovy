package com.scaledrop.sddownload.utilities

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS
import groovy.util.logging.Slf4j
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

@Slf4j
class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  public static final String FILE_UPDATES_QUEUE_URL = "file-updates-queue-url"

  public static final LocalStackContainer AWS_CONTAINER =  new LocalStackContainer(DockerImageName
      .parse("localstack/localstack:3.1.0"))
      .withClasspathResourceMapping("/localstack", "/etc/localstack/init/ready.d", BindMode.READ_ONLY)
      .withServices(SQS)
      .waitingFor(Wait.forLogMessage(".*Ready\\.\n", 1));

  static {
    AWS_CONTAINER.start();
  }

  @Override
  void initialize(ConfigurableApplicationContext applicationContext) {
    using().applyTo(applicationContext.getEnvironment());
  }

  static TestPropertyValues using() {
    List<String> pairs = new ArrayList<>()
    pairs.add("aws.region=" + AWS_CONTAINER.getRegion())
    pairs.add("aws.access-key-id=" + AWS_CONTAINER.getAccessKey())
    pairs.add("aws.secret-key=" + AWS_CONTAINER.getSecretKey())

    pairs.add("AWS_SQS_ENDPOINT=" + AWS_CONTAINER.getEndpointOverride(SQS))
    pairs.add("AWS_FILE_UPDATES_SQS_QUEUE_URL=" + AWS_CONTAINER.getEndpointOverride(SQS) + "/000000000000/" + FILE_UPDATES_QUEUE_URL)

    return TestPropertyValues.of(pairs)
  }
}
