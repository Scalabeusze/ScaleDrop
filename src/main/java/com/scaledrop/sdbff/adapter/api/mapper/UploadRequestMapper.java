package com.scaledrop.sdbff.adapter.api.mapper;

import com.scaledrop.sdbff.adapter.api.model.upload.request.RegisterUploadRequest;
import com.scaledrop.sdbff.domain.upload.UploadObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UploadRequestMapper {

  @Mapping(target = "ownerId", ignore = true)
  UploadObject toDomain(RegisterUploadRequest request);
}
