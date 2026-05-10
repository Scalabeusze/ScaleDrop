package com.scaledrop.sdupload.adapter.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "files")
public class FileEntity {

  @Id private UUID id;

  @Column(name = "owner_id", nullable = false)
  private UUID ownerId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "location", nullable = false)
  private String location;

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "size", nullable = false)
  private Long size;

  @Column(name = "hash")
  private String hash;

  @Column(name = "status", nullable = false)
  private String status;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;
}
