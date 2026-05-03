package com.scaledrop.sdbff.adapter.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class DownloadAPIResponse {

  @Schema(
      example = "https://s3.amazonaws.com/scaledrop/file-link-here...",
      description = "Pre-signed URL to download the file directly from S3")
  private String downloadUrl;

  @Schema(example = "important_document.pdf")
  private String fileName;

  @Schema(example = "image/png")
  private String contentType;
}
