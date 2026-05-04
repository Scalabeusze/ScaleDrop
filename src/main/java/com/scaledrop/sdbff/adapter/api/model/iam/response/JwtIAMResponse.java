package com.scaledrop.sdbff.adapter.api.model.iam.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing the generated session token")
public class JwtIAMResponse {

  @Schema(
      description = "Signed JWT token containing expiration and user data (id)",
      example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM...")
  private String jwt;
}
