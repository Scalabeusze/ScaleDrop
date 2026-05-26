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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileShareRepository extends JpaRepository<FileShareEntity, UUID> {

  List<FileShareEntity> findByFromIdAndToIdOrderByIdAsc(UUID fromId, UUID toId, Pageable pageable);

  List<FileShareEntity> findByFromIdOrderByIdAsc(UUID fromId, Pageable pageable);

  List<FileShareEntity> findByToIdOrderByIdAsc(UUID toId, Pageable pageable);

  List<FileShareEntity> findAllByOrderByIdAsc(Pageable pageable);

  Optional<FileShareEntity> findByFileIdAndFromIdAndToId(UUID fileId, UUID fromId, UUID toId);

  void deleteByFileId(UUID fileId);
}
