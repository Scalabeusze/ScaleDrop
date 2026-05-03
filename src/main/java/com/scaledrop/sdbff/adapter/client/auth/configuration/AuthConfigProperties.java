package com.scaledrop.sdbff.adapter.client.auth.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("iam-service")
public record AuthConfigProperties(
    @NotBlank String username,
    @NotBlank String password
) {}