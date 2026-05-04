package com.scaledrop.sdbff.adapter.api.model.iam.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request to obtain a JWT token")
public class SessionLoginIAMRequest {

  @Schema(description = "User login name", example = "tomasz.testowy")
  private String username;

  @Schema(description = "User password", example = "Haslo123!")
  private String password;
}
