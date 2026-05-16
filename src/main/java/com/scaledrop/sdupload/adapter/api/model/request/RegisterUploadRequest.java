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

package com.scaledrop.sdupload.adapter.api.model.request;

import com.scaledrop.sdupload.domain.upload.UploadType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to register file or folder metadata before upload/creation")
public class RegisterUploadRequest {

  @NotBlank(message = "Location cannot be blank") @Schema(description = "Target folder location", example = "/documents/")
  private String location;

  @NotBlank(message = "Name is required") @Schema(description = "Name of the file or folder", example = "Photos")
  private String name;

  @NotNull(message = "Type is required") @Schema(description = "Type of the object", example = "FILE")
  private UploadType type;

  @Schema(description = "MIME type of the file (optional for folders)", example = "image/png")
  private String contentType;

  @NotNull(message = "Size is required") @Schema(description = "Size in bytes (0 for folders)", example = "2048")
  private Long size;

  @Schema(
      description = "File hash for deduplication (optional for folders)",
      example = "e99a18c4...")
  private String hash;
}
