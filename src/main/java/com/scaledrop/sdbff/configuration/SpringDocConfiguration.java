package com.scaledrop.sdbff.configuration;

import static com.scaledrop.sdbff.configuration.Constants.BASIC_AUTH;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@OpenAPIDefinition(servers = {@Server(url = "${server.servlet.context-path}")})
public class SpringDocConfiguration {

  @Bean
  public OpenAPI springDocsOpenAPI(BuildProperties buildProperties) {
    Info info = new Info()
        .title("ScaleDrop BFF Service API")
        .version(buildProperties.getVersion());
    info.addExtension("x-build-time", buildProperties.getTime());

    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes(BASIC_AUTH, getBasicAuthScheme()))
        .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH))
        .info(info);
  }

  private SecurityScheme getBasicAuthScheme() {
    return new SecurityScheme()
        .type(Type.HTTP)
        .scheme("basic");
  }
}
