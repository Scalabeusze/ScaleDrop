package com.scaledrop.sdbff.domain.download;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FileObject(
    UUID fileId,
    UUID ownerId,
    String key,
    String name,
    String location,
    String contentType,
    Long size,
    String eTag,
    String status,
    OffsetDateTime lastModified) {}
