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
public class UploadObject {

  private UUID fileId;
  private String location;
  private String uploadUrl;
  private String status;
}
