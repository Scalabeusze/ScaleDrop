package com.scaledrop.sdupload.configuration.aws;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@Primary
@Component
@ToString
@ConfigurationProperties(prefix = "aws")
public class AmazonProperties {

  private String region;
  private String accessKeyId;
  @ToString.Exclude
  private String secretKey;

  @PostConstruct
  public void init() {
    log.info("AmazonProperties: {} ", this);
  }
}
