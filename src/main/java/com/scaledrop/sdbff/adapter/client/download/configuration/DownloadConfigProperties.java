package com.scaledrop.sdbff.adapter.client.download.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("download-service")
public record DownloadConfigProperties(
    @NotBlank String url,
    @NotBlank String username,
    @NotBlank String password
) {
}