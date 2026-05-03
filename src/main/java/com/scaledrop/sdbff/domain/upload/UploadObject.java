package com.scaledrop.sdbff.domain.upload;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UploadObject {
  private String fileName;
  private String contentType;
  private Long size;
  private UUID ownerId;
  private String hash;
}