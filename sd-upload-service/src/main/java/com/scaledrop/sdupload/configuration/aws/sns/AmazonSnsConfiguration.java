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

package com.scaledrop.sdupload.configuration.aws.sns;

import static org.apache.commons.lang3.ObjectUtils.allNotNull;

import com.scaledrop.sdupload.configuration.aws.AmazonProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Profile("!integration-test")
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AmazonSnsProperties.class)
public class AmazonSnsConfiguration {

  private final AmazonSnsProperties amazonSnsProperties;
  private final AmazonProperties amazonProperties;

  @Bean
  public SnsClient snsClient() {
    return apply(amazonSnsProperties, SnsClient.builder())
        .region(Region.of(amazonProperties.getRegion()))
        .credentialsProvider(buildCredentialsProvider())
        .build();
  }

  protected AwsCredentialsProvider buildCredentialsProvider() {
    if (allNotNull(amazonProperties.getAccessKeyId(), amazonProperties.getSecretKey())) {
      return () ->
          AwsBasicCredentials.create(
              amazonProperties.getAccessKeyId(), amazonProperties.getSecretKey());
    }
    return DefaultCredentialsProvider.builder().asyncCredentialUpdateEnabled(true).build();
  }

  private <T extends AwsClientBuilder<T, C>, C> AwsClientBuilder<T, C> apply(
      AmazonSnsProperties amazonProperties, AwsClientBuilder<T, C> builder) {
    if (amazonProperties.getEndpoint() != null) {
      builder.endpointOverride(amazonProperties.getEndpoint());
    }
    return builder;
  }
}
