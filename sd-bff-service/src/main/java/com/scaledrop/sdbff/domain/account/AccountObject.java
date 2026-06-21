package com.scaledrop.sdbff.domain.account;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountObject {
  private UUID id;
  private String username;
  private String status;
  private Integer failedLoginAttempts;
  private OffsetDateTime lockedUntil;
  private OffsetDateTime lastLoginAt;
  private OffsetDateTime passwordUpdatedAt;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}
