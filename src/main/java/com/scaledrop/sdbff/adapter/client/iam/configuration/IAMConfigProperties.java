package com.scaledrop.sdbff.adapter.client.iam.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("iam-service")
public class IAMConfigProperties {
  private String url;
  private String username;
  private String password;
}
