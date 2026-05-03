package com.scaledrop.sdbff.adapter.client.upload.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("upload-service")
public record UploadConfigProperties(@NotBlank String username, @NotBlank String password) {}
