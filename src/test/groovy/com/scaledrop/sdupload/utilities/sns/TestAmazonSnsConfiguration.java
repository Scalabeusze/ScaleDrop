package com.scaledrop.sdupload.utilities.sns;

import com.scaledrop.sdupload.configuration.aws.AmazonProperties;
import com.scaledrop.sdupload.configuration.aws.sns.AmazonSnsConfiguration;
import com.scaledrop.sdupload.configuration.aws.sns.AmazonSnsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.sns.SnsClient;

@Profile("integration-test")
@TestConfiguration
@RequiredArgsConstructor
public class TestAmazonSnsConfiguration {

  private final AmazonSnsProperties amazonSnsProperties;
  private final AmazonProperties amazonProperties;

  @Bean
  public SnsClient snsClient(SnsClientDecorator snsClientDecorator) {
    return snsClientDecorator;
  }

  @Bean
  public SnsClientDecorator snsClientDecorator() {
    SnsClient realClient = new AmazonSnsConfiguration(amazonSnsProperties, amazonProperties).snsClient();
    return new SnsClientDecorator(realClient);
  }
}
