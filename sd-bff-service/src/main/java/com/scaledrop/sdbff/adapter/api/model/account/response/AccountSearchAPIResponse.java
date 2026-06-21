package com.scaledrop.sdbff.adapter.api.model.account.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AccountSearchAPIResponse(
    @Schema(example = "498ecc77-a12c-409b-a37d-12631c75896c", description = "Account identifier")
        @NotNull UUID id,
    @Schema(example = "user@example.com", description = "Unique username for the account") @NotBlank String username) {}
