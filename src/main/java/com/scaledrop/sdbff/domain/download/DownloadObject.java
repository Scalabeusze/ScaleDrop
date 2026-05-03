package com.scaledrop.sdbff.domain.download;

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
public class DownloadObject {
  private String downloadUrl;
  private String fileName;
  private String contentType;
}
