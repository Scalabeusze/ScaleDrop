package com.scaledrop.sdbff.domain.download;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FileDownloadHistory(
    UUID downloadId,
    UUID fileId,
    UUID ownerId,
    OffsetDateTime requestedAt,
    OffsetDateTime expiresAt) {}
