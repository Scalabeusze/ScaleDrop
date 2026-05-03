package com.scaledrop.sdbff.adapter.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountAPIResponse {

    @Schema(
        example = "550e8400-e29b-41d4-a716-446655440000",
        description = "Unique identifier of the account"
    )
    private UUID accountId;

    @Schema(
        example = "test_user",
        description = "Username of the account holder"
    )
    private String username;

    @Schema(
        example = "test@scaledrop.com",
        description = "Email address of the account holder"
    )
    private String email;
}