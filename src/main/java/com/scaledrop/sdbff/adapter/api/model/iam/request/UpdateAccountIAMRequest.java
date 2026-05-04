package com.scaledrop.sdbff.adapter.api.model.iam.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update existing account data (excluding password)")
public class UpdateAccountIAMRequest {

  @Schema(description = "Updated username", example = "tomasz.nowy")
  private String username;

  @Schema(
      description = "New account status",
      example = "LOCKED",
      allowableValues = {"ACTIVE", "DISABLED", "LOCKED"})
  private String status;

  @Schema(description = "Manual override of the failed login attempts counter", example = "0")
  private Integer failedLoginAttempts;

  @Schema(description = "New account lockout expiration date/time")
  private OffsetDateTime lockedUntil;

  @Schema(description = "Time of the last successful login")
  private OffsetDateTime lastLoginAt;
}
