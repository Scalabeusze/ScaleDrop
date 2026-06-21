package com.scaledrop.sdbff.domain.upload;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadObject {
  private String location;
  private String name;
  private String contentType;
  private Long size;
  private String hash;
  private UploadType type;
  private UUID ownerId;
}
