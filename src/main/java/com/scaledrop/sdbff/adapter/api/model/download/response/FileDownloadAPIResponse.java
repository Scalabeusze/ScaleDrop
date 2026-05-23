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
public class FileDownloadAPIResponse {

  @Schema(description = "Download identifier", example = "f19b115d-96f1-4076-a965-a0291bc9c6e8")
  private UUID downloadId;

  @Schema(description = "File identifier", example = "498ecc77-a12c-409b-a37d-12631c75896c")
  private UUID fileId;

  @Schema(description = "Owner identifier", example = "d884ed12-cf74-4b57-8c31-fcb221d5dd50")
  private UUID ownerId;

  @Schema(description = "Timestamp when the download URL was requested")
  private OffsetDateTime requestedAt;

  @Schema(description = "Timestamp when the download URL expires")
  private OffsetDateTime expiresAt;
}
