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

package com.scaledrop.sdupload.utilities.sns;

import com.scaledrop.sdupload.configuration.aws.AmazonProperties;
import com.scaledrop.sdupload.configuration.aws.sns.AmazonSnsConfiguration;
import com.scaledrop.sdupload.configuration.aws.sns.AmazonSnsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.sns.SnsClient;

@Profile("integration-test")
@TestConfiguration
@RequiredArgsConstructor
public class TestAmazonSnsConfiguration {

  private final AmazonSnsProperties amazonSnsProperties;
  private final AmazonProperties amazonProperties;

  @Bean
  public SnsClient snsClient(SnsClientDecorator snsClientDecorator) {
    return snsClientDecorator;
  }

  @Bean
  public SnsClientDecorator snsClientDecorator() {
    SnsClient realClient =
        new AmazonSnsConfiguration(amazonSnsProperties, amazonProperties).snsClient();
    return new SnsClientDecorator(realClient);
  }
}
