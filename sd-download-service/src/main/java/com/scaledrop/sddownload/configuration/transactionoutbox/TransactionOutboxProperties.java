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

package com.scaledrop.sddownload.configuration.transactionoutbox;

import java.time.Duration;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.scheduling")
public class TransactionOutboxProperties {

  private Integer maxConcurrentTasks;
  private SchedulingProperties publishEvent;

  @Data
  public static class SchedulingProperties {

    private Boolean enabled;
    private String tableName;
    private String fixedDelay;
    private Duration attemptFrequency;
    private Integer blockAfterAttempts;
    private Integer flushBatchSize;
    private Boolean errorNotificationsEnabled;
  }
}
