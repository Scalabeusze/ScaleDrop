package com.scaledrop.sdbff.domain.download;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileShare {
  private UUID shareId;
  private UUID fileId;
  private UUID fromId;
  private UUID toId;
}
