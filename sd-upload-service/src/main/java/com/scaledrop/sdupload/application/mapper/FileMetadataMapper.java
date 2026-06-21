/*
 * Copyright 2026-present Scalabeusze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.scaledrop.sdupload.application.mapper;

import com.scaledrop.sdupload.adapter.db.FileEntity;
import com.scaledrop.sdupload.domain.upload.FileMetadata;
import org.springframework.stereotype.Component;

@Component
public class FileMetadataMapper {

  public FileMetadata toMetadata(FileEntity entity) {
    return FileMetadata.builder()
        .fileId(entity.getId())
        .ownerId(entity.getOwnerId())
        .name(entity.getName())
        .location(entity.getLocation())
        .contentType(entity.getContentType())
        .size(entity.getSize())
        .hash(entity.getHash())
        .status(entity.getStatus())
        .type(entity.getType())
        .build();
  }
}
