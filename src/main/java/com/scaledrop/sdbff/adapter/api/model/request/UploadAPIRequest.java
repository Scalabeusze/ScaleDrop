package com.scaledrop.sdbff.adapter.api.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadAPIRequest {

  @Schema(example = "document.pdf", description = "Name of the file")
  @NotBlank private String fileName;

  @Schema(example = "application/pdf", description = "MIME type")
  @NotBlank private String contentType;

  @Schema(example = "1048576", description = "File size in bytes")
  @NotNull @Positive private Long size;

  @Schema(example = "e99a18c428cb38d5f260853678922e03", description = "File checksum")
  @NotBlank private String hash;
}
