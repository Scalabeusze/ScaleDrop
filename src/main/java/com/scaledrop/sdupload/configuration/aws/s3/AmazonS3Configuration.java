package com.scaledrop.sdupload.configuration.aws.s3;

import com.scaledrop.sdupload.configuration.aws.AmazonProperties;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AmazonS3Properties.class)
public class AmazonS3Configuration {

  private static final String SESSION_ROLE_NAME = "sd-upload-service";

  private final AmazonS3Properties amazonS3Properties;
  private final AmazonProperties amazonProperties;

  @Bean
  public S3Client s3Client() {
    return apply(amazonS3Properties.getFileserver().getEndpoint(), S3Client.builder())
        .region(Region.of(amazonS3Properties.getFileserver().getRegion()))
        .credentialsProvider(buildCredentialsProvider())
        .build();
  }


  @Bean
  public S3AsyncClient s3AsyncClient(AwsCredentialsProvider awsCredentialsProvider) {
    return apply(amazonS3Properties.getFileserver().getEndpoint(), S3AsyncClient.builder())
        .region(Region.of(amazonS3Properties.getFileserver().getRegion()))
        .credentialsProvider(awsCredentialsProvider)
        .serviceConfiguration(builder -> builder.checksumValidationEnabled(false))
        .build();
  }

  @Bean
  public AwsCredentialsProvider awsCredentialsProvider() {
    log.info("Assuming role {}", amazonS3Properties.getAssumeRole());

    if (StringUtils.isNotEmpty(amazonS3Properties.getAssumeRole())) {
      StsClient stsClient = StsClient.builder()
          .credentialsProvider(buildCredentialsProvider())
          .region(Region.of(amazonProperties.getRegion()))
          .build();

      return StsAssumeRoleCredentialsProvider.builder()
          .stsClient(stsClient)
          .asyncCredentialUpdateEnabled(true)
          .refreshRequest(refreshRequestBuilder -> refreshRequestBuilder
              .roleArn(amazonS3Properties.getAssumeRole())
              .roleSessionName(SESSION_ROLE_NAME)
              .build())
          .build();
    }

    return buildCredentialsProvider();
  }

  private DefaultCredentialsProvider buildCredentialsProvider() {
    return DefaultCredentialsProvider.builder()
        .asyncCredentialUpdateEnabled(true)
        .build();
  }

  /**
   * Works like filter - apply common logic with overriding endpoint for all builders that extends AwsClientBuilder
   *
   * @param endpoint endpoint which overrides default AWS endpoint
   * @param builder  Aws client builder that extends AwsClientBuilder
   * @param <T>      - BuilderT generic version
   * @param <C>      - ClientT generic version
   * @return AwsClientBuilder
   */
  private <T extends AwsClientBuilder<T, C>, C> AwsClientBuilder<T, C> apply(String endpoint, AwsClientBuilder<T, C> builder) {
    if (StringUtils.isNotBlank(endpoint)) {
      builder.endpointOverride(URI.create(endpoint));
    }
    return builder;
  }
}
