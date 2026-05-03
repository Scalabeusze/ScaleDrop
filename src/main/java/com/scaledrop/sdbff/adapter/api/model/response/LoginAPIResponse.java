package com.scaledrop.sdbff.adapter.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginAPIResponse {

  @Schema(description = "JWT Access Token")
  private String accessToken;

  @Schema(description = "JWT Refresh Token")
  private String refreshToken;

  @Schema(description = "Token expiration time in seconds")
  private Long expiresIn;
}
