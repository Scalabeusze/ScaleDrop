package com.scaledrop.sdbff.adapter.api.model.download.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAPIResponse {

  @Schema(description = "File identifier", example = "498ecc77-a12c-409b-a37d-12631c75896c")
  private UUID fileId;

  @Schema(description = "Owner identifier", example = "d884ed12-cf74-4b57-8c31-fcb221d5dd50")
  private UUID ownerId;

  @Schema(description = "S3 object key", example = "ersms/lectures/01-introduction.md")
  private String key;

  @Schema(description = "Original file name", example = "01-introduction.md")
  private String name;

  @Schema(description = "Original file location", example = "/ersms/lectures/")
  private String location;

  @Schema(description = "File content type", example = "text/markdown")
  private String contentType;

  @Schema(description = "Object size in bytes", example = "578")
  private Long size;

  @Schema(description = "S3 object ETag", example = "9b2cf535f27731c974343645a3985328")
  private String eTag;

  @Schema(description = "Upload status", example = "UPLOADED")
  private String status;

  @Schema(description = "Timestamp when the object was last modified")
  private OffsetDateTime lastModified;
}
