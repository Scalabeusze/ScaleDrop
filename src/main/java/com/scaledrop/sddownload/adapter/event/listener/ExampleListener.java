package com.scaledrop.sddownload.adapter.event.listener;

import com.gruelbox.transactionoutbox.TransactionOutbox;
import com.scaledrop.sddownload.domain.example.ExampleObject;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExampleListener {

  private final TransactionOutbox transactionOutbox;

  @Transactional
  @SqsListener(value = "${aws.sqs.fileUpdatesQueueUrl}", acknowledgementMode = SqsListenerAcknowledgementMode.ON_SUCCESS)
  public void onEvent(ExampleObject event) {
    transactionOutbox.schedule(this.getClass()).processEvent(event);
  }

  public void processEvent(ExampleObject event) {
    log.info("[EXAMPLE] Processing example event: {}", event);
  }
}
