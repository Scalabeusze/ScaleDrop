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

package com.scaledrop.sdiam.adapter.api.model.request;

import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record UpdateAccountAPIRequest(
    @Schema(
            example = "Tomasz Testowy",
            description = "Unique username for the account",
            requiredMode = RequiredMode.NOT_REQUIRED)
        @Size(min = 1, max = 100) String username,
    @Schema(
            example = "ACTIVE",
            description = "Account lifecycle status",
            requiredMode = RequiredMode.NOT_REQUIRED)
        AccountStatus status,
    @Schema(
            description = "Timestamp of the last successful login",
            requiredMode = RequiredMode.NOT_REQUIRED)
        OffsetDateTime lastLoginAt) {

  public UpdateAccountAPIRequest {
    if (username != null) {
      username = username.trim();
    }
  }
}
