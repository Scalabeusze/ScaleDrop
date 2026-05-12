package com.scaledrop.sdbff.adapter.api.model.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Builder
@ToString
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class LoginAPIRequest {

  @NotBlank @Schema(
      requiredMode = RequiredMode.REQUIRED,
      description =
          "Google ID token obtained from the client after successful Google authentication")
  private String googleIdToken;
}
