package com.scaledrop.sdbff.adapter.api.model.download.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FileShareResponse(
    @Schema(example = "27d6e620-46c3-4a31-8115-74c850a27c0b", description = "Share identifier")
        @NotNull UUID shareId,
    @Schema(example = "498ecc77-a12c-409b-a37d-12631c75896c", description = "File identifier")
        @NotNull UUID fileId,
    @Schema(
            example = "d884ed12-cf74-4b57-8c31-fcb221d5dd50",
            description = "Share owner identifier")
        @NotNull UUID fromId,
    @Schema(
            example = "5d0049ab-e1f0-4328-b341-e68caad01e99",
            description = "Share recipient identifier")
        @NotNull UUID toId) {}
