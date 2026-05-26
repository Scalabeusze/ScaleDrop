/*
 * Copyright 2026-present Scalabeusze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.scaledrop.sdupload.utilities

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS

import groovy.util.logging.Slf4j
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile

@Slf4j
class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  // DEFINICJA STAŁEJ - musi być tutaj, aby była widoczna dla metody using()
  public static final String FILESERVER_BUCKET_NAME = "sd-fileserver-test"

  public static final LocalStackContainer AWS_CONTAINER = new LocalStackContainer(
  DockerImageName.parse("localstack/localstack:3.1.0")
  )
  .withCopyFileToContainer(
  MountableFile.forClasspathResource("localstack/init.sh", 0777),
  "/etc/localstack/init/ready.d/init.sh"
  )
  .withServices(SQS, SNS, S3)
  .waitingFor(Wait.forLogMessage(".*Ready\\.\n", 1))

  static {
    AWS_CONTAINER.start()
  }

  @Override
  void initialize(ConfigurableApplicationContext applicationContext) {
    using().applyTo(applicationContext.getEnvironment())
  }

  static TestPropertyValues using() {
    List<String> pairs = new ArrayList<>()

    // AWS Global credentials
    pairs.add("aws.region=" + AWS_CONTAINER.getRegion())
    pairs.add("aws.access-key-id=" + AWS_CONTAINER.getAccessKey())
    pairs.add("aws.secret-key=" + AWS_CONTAINER.getSecretKey())

    // AWS SNS configuration
    pairs.add("aws.sns.endpoint=" + AWS_CONTAINER.getEndpointOverride(SNS).toString())
    pairs.add("aws.sns.file-updates-topic-arn=" + "arn:aws:sns:" + AWS_CONTAINER.getRegion() + ":000000000000:file-updates-topic-arn")

    // AWS S3 configuration
    pairs.add("aws.s3.fileserver.endpoint=" + AWS_CONTAINER.getEndpointOverride(S3).toString())
    pairs.add("aws.s3.fileserver.region=" + AWS_CONTAINER.getRegion())
    pairs.add("aws.s3.fileserver.bucket=" + FILESERVER_BUCKET_NAME)

    // Debug logging for AWS modules
    pairs.add("logging.level.com.scaledrop.sdupload.adapter.aws=DEBUG")
    pairs.add("logging.level.software.amazon.awssdk.services.s3=DEBUG")

    return TestPropertyValues.of(pairs)
  }
}
