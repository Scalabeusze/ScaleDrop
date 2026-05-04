package com.scaledrop.sdbff.adapter.api.mapper;

import com.scaledrop.sdbff.adapter.api.model.upload.request.UploadAPIRequest;
import com.scaledrop.sdbff.configuration.MapperConfiguration;
import com.scaledrop.sdbff.domain.upload.UploadObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface UploadRequestMapper {
  @Mapping(target = "ownerId", ignore = true)
  UploadObject toDomain(UploadAPIRequest request);
}
