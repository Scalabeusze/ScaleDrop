package com.scaledrop.sdbff.adapter.client.account.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("iam-service")
public record AccountConfigProperties(@NotBlank String username, @NotBlank String password) {}
