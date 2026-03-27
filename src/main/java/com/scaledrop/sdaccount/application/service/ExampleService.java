package com.scaledrop.sdaccount.application.service;

import com.scaledrop.sdaccount.application.port.in.ExampleUseCase;
import com.scaledrop.sdaccount.domain.example.ExampleObject;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExampleService implements ExampleUseCase {

  @Override
  public ExampleObject getExampleObject() {
    log.warn("[EXAMPLE] Received request to get example object");
    return ExampleObject.builder()
        .exampleId(UUID.randomUUID())
        .exampleField("Some example value")
        .build();
  }
}
