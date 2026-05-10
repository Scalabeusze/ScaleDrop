package com.scaledrop.sdupload.adapter.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleAPIResponse {

  @Schema(
      example = "498ecc77-a12c-409b-a37d-12631c75896c",
      description = "This is an example UUID field in the API response")
  UUID exampleId;

  @Schema(
      example = "Some example value",
      description = "This is an example field in the API response")
  String exampleField;
}
