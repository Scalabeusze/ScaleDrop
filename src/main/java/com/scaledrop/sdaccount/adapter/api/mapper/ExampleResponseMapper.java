package com.scaledrop.sdaccount.adapter.api.mapper;

import com.scaledrop.sdaccount.adapter.api.model.response.ExampleAPIResponse;
import com.scaledrop.sdaccount.configuration.MapperConfiguration;
import com.scaledrop.sdaccount.domain.example.ExampleObject;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface ExampleResponseMapper {

  ExampleAPIResponse toResponse(ExampleObject exampleObject);
}
