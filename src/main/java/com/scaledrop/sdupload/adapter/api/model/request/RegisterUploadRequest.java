package com.scaledrop.sdupload.adapter.api.model.request;

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
@Schema(description = "Request to register file metadata before physical upload")
public class RegisterUploadRequest {

  @NotBlank(message = "Location cannot be blank") @Schema(description = "Target folder location (e.g., '/', '/documents/')", example = "/")
  private String location;

  @NotBlank(message = "File name is required") @Schema(description = "Name of the file with extension", example = "document.pdf")
  private String name;

  @NotBlank(message = "Content type is required") @Schema(description = "MIME type of the file", example = "application/pdf")
  private String contentType;

  @NotNull(message = "File size is required") @Schema(description = "Size of the file in bytes", example = "1048576")
  private Long size;

  @NotBlank(message = "File hash is required") @Schema(description = "SHA-256 or MD5 hash for deduplication", example = "a1b2c3d4e5f6...")
  private String hash;
}
