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
@Schema(description = "Request to change account password")
public class UpdatePasswordIAMRequest {

  @Schema(
      description = "New plain text password, which will be hashed in IAM",
      example = "NoweSuperHaslo456!")
  private String plainPassword;
}
