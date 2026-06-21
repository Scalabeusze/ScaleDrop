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

package com.scaledrop.sdupload.configuration.aws.s3;

import com.scaledrop.sdupload.configuration.aws.AmazonProperties;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AmazonS3Properties.class)
public class AmazonS3Configuration {

  private static final String SESSION_ROLE_NAME = "sd-upload-service";

  private final AmazonS3Properties amazonS3Properties;
  private final AmazonProperties amazonProperties;

  @Bean
  public S3Client s3Client() {
    var builder =
        S3Client.builder()
            .region(Region.of(amazonS3Properties.getFileserver().getRegion()))
            .credentialsProvider(buildCredentialsProvider())
            .serviceConfiguration(s3ServiceConfiguration());

    if (isCustomEndpointConfigured()) {
      builder.endpointOverride(URI.create(amazonS3Properties.getFileserver().getEndpoint()));
    }

    return builder.build();
  }

  @Bean
  public S3AsyncClient s3AsyncClient(AwsCredentialsProvider awsCredentialsProvider) {
    var builder =
        S3AsyncClient.builder()
            .region(Region.of(amazonS3Properties.getFileserver().getRegion()))
            .credentialsProvider(awsCredentialsProvider)
            .serviceConfiguration(
                configuration ->
                    configuration
                        .checksumValidationEnabled(false)
                        .pathStyleAccessEnabled(isCustomEndpointConfigured()));

    if (isCustomEndpointConfigured()) {
      builder.endpointOverride(URI.create(amazonS3Properties.getFileserver().getEndpoint()));
    }

    return builder.build();
  }

  @Bean
  public AwsCredentialsProvider awsCredentialsProvider() {
    log.info("Assuming role {}", amazonS3Properties.getAssumeRole());

    if (StringUtils.isNotEmpty(amazonS3Properties.getAssumeRole())) {
      StsClient stsClient =
          StsClient.builder()
              .credentialsProvider(buildCredentialsProvider())
              .region(Region.of(amazonProperties.getRegion()))
              .build();

      return StsAssumeRoleCredentialsProvider.builder()
          .stsClient(stsClient)
          .asyncCredentialUpdateEnabled(true)
          .refreshRequest(
              refreshRequestBuilder ->
                  refreshRequestBuilder
                      .roleArn(amazonS3Properties.getAssumeRole())
                      .roleSessionName(SESSION_ROLE_NAME)
                      .build())
          .build();
    }

    return buildCredentialsProvider();
  }

  @Bean
  public S3Presigner s3Presigner(AwsCredentialsProvider awsCredentialsProvider) {
    S3Presigner.Builder builder =
        S3Presigner.builder()
            .region(Region.of(amazonS3Properties.getFileserver().getRegion()))
            .credentialsProvider(awsCredentialsProvider)
            .serviceConfiguration(s3ServiceConfiguration());

    if (isCustomEndpointConfigured()) {
      log.info(
          "[S3-CONFIG] Applying local endpoint override: {}",
          amazonS3Properties.getFileserver().getEndpoint());
      builder.endpointOverride(URI.create(amazonS3Properties.getFileserver().getEndpoint()));
    }

    return builder.build();
  }

  private S3Configuration s3ServiceConfiguration() {
    return S3Configuration.builder().pathStyleAccessEnabled(isCustomEndpointConfigured()).build();
  }

  private boolean isCustomEndpointConfigured() {
    return StringUtils.isNotBlank(amazonS3Properties.getFileserver().getEndpoint());
  }

  private AwsCredentialsProvider buildCredentialsProvider() {
    if (ObjectUtils.allNotNull(
        amazonProperties.getAccessKeyId(), amazonProperties.getSecretKey())) {
      return () ->
          AwsBasicCredentials.create(
              amazonProperties.getAccessKeyId(), amazonProperties.getSecretKey());
    }
    return DefaultCredentialsProvider.builder().asyncCredentialUpdateEnabled(true).build();
  }
}
