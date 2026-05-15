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

package com.scaledrop.sddownload.adapter.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
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
@Entity
@Table(name = "files")
public class FileEntity {

  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(nullable = false, unique = true, columnDefinition = "TEXT")
  private String key;

  @Column(nullable = false)
  private Long size;

  @Column(name = "last_modified", nullable = false)
  private OffsetDateTime lastModified;

  @Column(name = "e_tag", nullable = false, length = 255)
  private String eTag;

  @Column(name = "owner_id")
  private UUID ownerId;

  @Column(length = 255)
  private String name;

  @Column(length = 1024)
  private String location;

  @Column(name = "content_type", length = 128)
  private String contentType;

  @Column(length = 32)
  private String status;
}
