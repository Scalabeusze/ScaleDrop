package com.scaledrop.sdupload.configuration.aws.sns;

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
@ConfigurationProperties(prefix = "aws.sns")
public class AmazonSnsProperties {
  private URI endpoint;
  private String fileUpdatesTopicArn;
}
