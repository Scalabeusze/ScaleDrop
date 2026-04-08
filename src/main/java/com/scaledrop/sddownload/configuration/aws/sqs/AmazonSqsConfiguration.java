package com.scaledrop.sddownload.configuration.aws.sqs;

import static org.apache.commons.lang3.ObjectUtils.allNotNull;

import com.scaledrop.sddownload.configuration.aws.AmazonProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AmazonSqsProperties.class)
public class AmazonSqsConfiguration {

  private final AmazonSqsProperties amazonSqsProperties;
  private final AmazonProperties amazonProperties;

  @Bean
  public SqsAsyncClient sqsAsyncClient() {
    return apply(amazonSqsProperties, SqsAsyncClient.builder())
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
  apply(AmazonSqsProperties amazonProperties, AwsClientBuilder<T, C> builder) {
    if (amazonProperties.getEndpoint() != null) {
      builder.endpointOverride(amazonProperties.getEndpoint());
    }
    return builder;
  }
}
