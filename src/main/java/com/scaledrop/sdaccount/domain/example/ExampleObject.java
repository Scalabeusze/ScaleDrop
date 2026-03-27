package com.scaledrop.sdaccount.domain.example;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class ExampleObject {

  private final UUID exampleId;
  private final String exampleField;
}
