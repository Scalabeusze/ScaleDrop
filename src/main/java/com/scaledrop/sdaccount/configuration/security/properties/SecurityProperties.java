package com.scaledrop.sdaccount.configuration.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("security.access")
public class SecurityProperties {

  private BasicAuthorization documentation;
  private BasicAuthorization internal;

  @Setter
  @Getter
  public static class BasicAuthorization {

    String username;
    String password;
  }
}
