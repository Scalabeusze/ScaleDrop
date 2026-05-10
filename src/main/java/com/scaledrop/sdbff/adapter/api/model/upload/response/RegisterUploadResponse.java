package com.scaledrop.sdbff.adapter.api.model.upload.response;

import com.scaledrop.sdbff.domain.upload.UploadType;
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
@Schema(description = "Response containing upload data")
public class RegisterUploadResponse {

  @Schema(
      description = "Unique identifier of the record",
      example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID fileId;

  @Schema(description = "Final path", example = "/documents/manual.pdf")
  private String location;

  @Schema(description = "Pre-signed URL (null if FOLDER)")
  private String uploadUrl;

  @Schema(description = "Current status", example = "PENDING")
  private String status;

  @Schema(description = "Confirmed type", example = "FILE")
  private UploadType type;
}
