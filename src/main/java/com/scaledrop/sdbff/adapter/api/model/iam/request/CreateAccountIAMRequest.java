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
@Schema(description = "Request to create a new user account")
public class CreateAccountIAMRequest {

  @Schema(description = "Unique username", example = "tomasz.testowy")
  private String username;

  @Schema(description = "Plain text password", example = "Haslo123!")
  private String plainPassword;

  @Schema(
      description = "Account status",
      example = "ACTIVE",
      allowableValues = {"ACTIVE", "DISABLED", "LOCKED"})
  private String status;

  @Schema(description = "Date until which the account remains locked (optional)")
  private OffsetDateTime lockedUntil;
}
