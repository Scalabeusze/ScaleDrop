package com.scaledrop.sdbff.adapter.api.mapper;

import com.scaledrop.sdbff.adapter.api.model.response.ExampleAPIResponse;
import com.scaledrop.sdbff.configuration.MapperConfiguration;
import com.scaledrop.sdbff.domain.example.ExampleObject;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface ExampleResponseMapper {

  ExampleAPIResponse toResponse(ExampleObject exampleObject);
}
