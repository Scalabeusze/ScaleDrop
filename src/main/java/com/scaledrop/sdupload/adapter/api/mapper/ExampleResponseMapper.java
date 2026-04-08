package com.scaledrop.sdupload.adapter.api.mapper;

import com.scaledrop.sdupload.adapter.api.model.response.ExampleAPIResponse;
import com.scaledrop.sdupload.configuration.MapperConfiguration;
import com.scaledrop.sdupload.domain.example.ExampleObject;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface ExampleResponseMapper {

  ExampleAPIResponse toResponse(ExampleObject exampleObject);
}
