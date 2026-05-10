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
public class RegisterUploadResponse {

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
