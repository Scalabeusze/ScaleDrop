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
import com.scaledrop.sdiam.configuration.annotations.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public record CreateAccountAPIRequest(
    @Schema(example = "Tomasz Testowy", description = "Unique username for the account") @NotBlank String username,
    @Schema(example = "plain-text-password", description = "Plain-text password to be hashed")
        @ValidPassword
        String plainPassword,
    @Schema(example = "ACTIVE", description = "Account lifecycle status") AccountStatus status,
    @Schema(description = "Lock expiration timestamp if the account is temporarily locked")
        OffsetDateTime lockedUntil) {

  public CreateAccountAPIRequest {
    if (username != null) {
      username = username.trim();
    }
  }
}
