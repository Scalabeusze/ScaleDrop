package com.scaledrop.sdupload.domain.upload;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
  private UUID fileId;
  private UUID ownerId;
  private String name;
  private String location;
  private String contentType;
  private Long size;
  private String hash;
  private String status;
  private UploadType type;
}
