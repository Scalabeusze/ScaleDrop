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

package com.scaledrop.sdupload.adapter.api.model.response;

import com.scaledrop.sdupload.domain.upload.UploadType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Response containing the registration details and S3 upload URL if applicable")
public class UploadResponse {

  @Schema(
      description = "Unique identifier of the registered object (file or folder) in the database",
      example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID fileId;

  @Schema(
      description = "The parent location/path where the object was registered",
      example = "/moje-zdjecia/")
  private String location;

  @Schema(
      description = "Pre-signed URL for S3 upload. Will be null for folders.",
      example = "https://s3.amazonaws.com/...")
  private String uploadUrl;

  @Schema(description = "Current status of the registration", example = "PENDING")
  private String status;

  @Schema(description = "Type of the registered object", example = "FILE")
  private UploadType type;
}
