package com.scaledrop.sdupload.adapter.api.model.response;

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
@Schema(description = "Response containing the upload URL and generated file ID")
public class RegisterUploadResponse {

  @Schema(
      description = "Unique identifier of the registered file in Database",
      example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID fileId;

  @Schema(description = "Final path of the file", example = "/wakacje/123.jpg")
  private String location;

  @Schema(
      description = "Pre-signed URL to perform a PUT request with the physical file directly to S3")
  private String uploadUrl;

  @Schema(description = "Current status of the file", example = "PENDING")
  private String status;
}
