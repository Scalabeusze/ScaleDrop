package com.scaledrop.sddownload.configuration.aws.s3;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@Component
@ToString
@ConfigurationProperties(prefix = "aws.s3")
public class AmazonS3Properties {

  private String assumeRole;

  private BucketProperties fileserver;

  @Getter
  @Setter
  @ToString
  public static class BucketProperties {
    private String bucket;
    private String endpoint;
    private String region;
  }

  @PostConstruct
  public void init() {
    log.info("AmazonS3Properties: {}", this);
  }

}
