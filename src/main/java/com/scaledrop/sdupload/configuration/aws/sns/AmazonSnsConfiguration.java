package com.scaledrop.sdupload.configuration.aws.sns;

import static org.apache.commons.lang3.ObjectUtils.allNotNull;

import com.scaledrop.sdupload.configuration.aws.AmazonProperties;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Profile("!integration-test")
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AmazonSnsProperties.class)
public class AmazonSnsConfiguration {

  private final AmazonSnsProperties amazonSnsProperties;
  private final AmazonProperties amazonProperties;

  @Bean
  public SnsClient snsClient() {
    return apply(amazonSnsProperties, SnsClient.builder())
        .region(Region.of(amazonProperties.getRegion()))
        .credentialsProvider(buildCredentialsProvider())
        .build();
  }

  protected AwsCredentialsProvider buildCredentialsProvider() {
    if (allNotNull(amazonProperties.getAccessKeyId(), amazonProperties.getSecretKey())) {
      return () -> AwsBasicCredentials.create(amazonProperties.getAccessKeyId(), amazonProperties.getSecretKey());
    }
    return DefaultCredentialsProvider.builder()
        .asyncCredentialUpdateEnabled(true)
        .build();
  }

  private <T extends AwsClientBuilder<T, C>, C> AwsClientBuilder<T, C>
  apply(AmazonSnsProperties amazonProperties, AwsClientBuilder<T, C> builder) {
    if (amazonProperties.getEndpoint() != null) {
      builder.endpointOverride(amazonProperties.getEndpoint());
    }
    return builder;
  }
}
