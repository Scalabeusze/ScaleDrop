package com.scaledrop.sdbff.adapter.api.model.iam.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response containing detailed account data")
public class AccountIAMResponse {

  @Schema(
      description = "Unique account identifier (UUID)",
      example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID id;

  @Schema(description = "Username", example = "tomasz.testowy")
  private String username;

  @Schema(description = "Current account status", example = "ACTIVE")
  private String status;

  @Schema(description = "Number of failed login attempts since the last success", example = "0")
  private Integer failedLoginAttempts;

  @Schema(description = "If the account is locked, indicates until when. Empty if not locked.")
  private OffsetDateTime lockedUntil;

  @Schema(description = "Date and time of the last successful login")
  private OffsetDateTime lastLoginAt;

  @Schema(description = "Date and time of the last password change")
  private OffsetDateTime passwordUpdatedAt;

  @Schema(description = "Date and time of account creation in the system")
  private OffsetDateTime createdAt;

  @Schema(description = "Date and time of the last modification of any account field")
  private OffsetDateTime updatedAt;
}
