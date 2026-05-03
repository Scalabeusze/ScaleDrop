package com.scaledrop.sdbff.adapter.api.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginAPIRequest {

    @NotBlank
    @Schema(
        description = "Authorization code received from Google OAuth2",
        example = "4/0AdQt8qh..."
    )
    private String gcode;
}