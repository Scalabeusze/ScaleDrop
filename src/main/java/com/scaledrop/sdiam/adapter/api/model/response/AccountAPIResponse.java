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

package com.scaledrop.sdiam.adapter.api.model.response;

import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountAPIResponse(
    @Schema(example = "498ecc77-a12c-409b-a37d-12631c75896c", description = "Account identifier")
        @NotNull UUID id,
    @Schema(
            example = "marian.pazdzioch@swiatwgkiepskich.com",
            description = "Unique username for the account")
        @NotBlank String username,
    @Schema(example = "Marian", description = "Users first name") String firstName,
    @Schema(example = "Pazdzioch", description = "Users last name") String lastName,
    @Schema(example = "https://www.example.com", description = "URL to the user's avatar image")
        String avatarUrl,
    @Schema(example = "ACTIVE", description = "Account lifecycle status") @NotNull AccountStatus status,
    @Schema(description = "Timestamp of the last successful login") OffsetDateTime lastLoginAt,
    @Schema(description = "Timestamp when the account was created") @NotNull OffsetDateTime createdAt,
    @Schema(description = "Timestamp when the account was last updated") @NotNull OffsetDateTime updatedAt) {}
