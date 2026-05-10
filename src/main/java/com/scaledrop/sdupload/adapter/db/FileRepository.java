package com.scaledrop.sdupload.adapter.db;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {
  boolean existsByOwnerIdAndHashAndLocationAndName(
      UUID ownerId, String hash, String location, String name);
}
