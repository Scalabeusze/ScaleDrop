package com.scaledrop.sdupload.utilities.sns;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@AllArgsConstructor
public class SnsClientDecorator implements SnsClient {

  private final List<PublishRequest> publishedRequests = new CopyOnWriteArrayList<>();

  @Delegate
  private final SnsClient snsClient;

  @Override
  public PublishResponse publish(PublishRequest publishRequest) throws SdkException {
    PublishResponse publish = snsClient.publish(publishRequest);
    publishedRequests.add(publishRequest);
    return publish;
  }

  public void clearPublishedRequests() {
    publishedRequests.clear();
  }

  public List<PublishRequest> getPublishedRequests() {
    return new ArrayList<>(publishedRequests);
  }
}
