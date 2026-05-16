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

package com.scaledrop.sddownload.adapter.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FileDownloadAPIResponse(
    @Schema(example = "f19b115d-96f1-4076-a965-a0291bc9c6e8", description = "Download identifier")
        @NotNull UUID downloadId,
    @Schema(example = "498ecc77-a12c-409b-a37d-12631c75896c", description = "File identifier")
        @NotNull UUID fileId,
    @Schema(example = "d884ed12-cf74-4b57-8c31-fcb221d5dd50", description = "Owner identifier")
        UUID ownerId,
    @Schema(description = "Timestamp when the download URL was requested") @NotNull OffsetDateTime requestedAt,
    @Schema(description = "Timestamp when the download URL expires") @NotNull OffsetDateTime expiresAt) {}
