package com.scaledrop.sdbff.adapter.api.model.upload.request;

import com.scaledrop.sdbff.domain.upload.UploadType;
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
@Schema(description = "Request to register file or folder metadata")
public class RegisterUploadRequest {

  @NotBlank(message = "Location cannot be blank") @Schema(description = "Target folder location", example = "/")
  private String location;

  @NotBlank(message = "Name is required") @Schema(description = "Name of the file or folder", example = "document.pdf")
  private String name;

  @NotBlank(message = "Content type is required") @Schema(description = "MIME type", example = "application/pdf")
  private String contentType;

  @NotNull(message = "Size is required") @Schema(description = "Size in bytes (0 for folders)", example = "1048576")
  private Long size;

  @Schema(description = "File hash (optional for folders)", example = "a1b2c3d4e5f6...")
  private String hash;

  @NotNull(message = "Type is required") @Schema(description = "Type of object: FILE or FOLDER", example = "FILE")
  private UploadType type;
}
