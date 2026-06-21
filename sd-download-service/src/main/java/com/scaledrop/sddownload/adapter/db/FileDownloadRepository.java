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
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileDownloadRepository extends JpaRepository<FileDownloadEntity, UUID> {

  @Query(
      """
      SELECT d.id AS downloadId,
             d.fileId AS fileId,
             f.ownerId AS ownerId,
             d.requestedAt AS requestedAt,
             d.expiresAt AS expiresAt
        FROM FileDownloadEntity d
        JOIN FileEntity f ON f.id = d.fileId
       WHERE (:fileId IS NULL OR d.fileId = :fileId)
         AND (:ownerId IS NULL OR f.ownerId = :ownerId)
       ORDER BY d.requestedAt DESC, d.id ASC
      """)
  List<FileDownloadHistoryProjection> findHistory(
      @Param("fileId") UUID fileId, @Param("ownerId") UUID ownerId, Pageable pageable);
}
