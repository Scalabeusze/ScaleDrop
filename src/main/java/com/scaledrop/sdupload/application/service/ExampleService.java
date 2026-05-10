package com.scaledrop.sdupload.application.service;

import com.scaledrop.sdupload.adapter.event.publisher.ExamplePublisher;
import com.scaledrop.sdupload.application.port.in.ExampleUseCase;
import com.scaledrop.sdupload.domain.example.ExampleObject;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExampleService implements ExampleUseCase {

  private final ExamplePublisher examplePublisher;

  @Override
  public ExampleObject getExampleObject() {
    log.warn("[EXAMPLE] Received request to get example object");
    ExampleObject exampleObject =
        ExampleObject.builder()
            .exampleId(UUID.randomUUID())
            .exampleField("Some example value")
            .build();

    examplePublisher.publishEvent(exampleObject);
    return exampleObject;
  }
}
