package com.scaledrop.sddownload.adapter.api.mapper;

import com.scaledrop.sddownload.adapter.api.model.response.ExampleAPIResponse;
import com.scaledrop.sddownload.configuration.MapperConfiguration;
import com.scaledrop.sddownload.domain.example.ExampleObject;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface ExampleResponseMapper {

  ExampleAPIResponse toResponse(ExampleObject exampleObject);
}
