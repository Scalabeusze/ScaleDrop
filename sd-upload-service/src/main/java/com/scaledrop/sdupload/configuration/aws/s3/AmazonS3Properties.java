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

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@Component
@ToString
@ConfigurationProperties(prefix = "aws.s3")
public class AmazonS3Properties {

  private String assumeRole;

  private BucketProperties fileserver;

  @Getter
  @Setter
  @ToString
  public static class BucketProperties {
    private String bucket;
    private String endpoint;
    private String region;
  }

  @PostConstruct
  public void init() {
    log.info("AmazonS3Properties: {}", this);
  }
}
