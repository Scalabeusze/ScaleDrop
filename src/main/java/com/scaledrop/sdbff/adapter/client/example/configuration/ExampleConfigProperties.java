package com.scaledrop.sdbff.adapter.client.example.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("example-service")
public record ExampleConfigProperties(
    @NotBlank String username,
    @NotBlank String password
) {

}
