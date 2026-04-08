package com.scaledrop.sddownload.configuration.aws.sqs;

import jakarta.annotation.PostConstruct;
import java.net.URI;
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
@ConfigurationProperties(prefix = "aws.sqs")
public class AmazonSqsProperties {

  private URI endpoint;
  private String fileUpdatesQueueUrl;

  @PostConstruct
  public void init() {
    log.info("AmazonSqsProperties: {} ", this);
  }
}
