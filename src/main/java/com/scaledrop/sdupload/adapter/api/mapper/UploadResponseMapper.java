package com.scaledrop.sdupload.adapter.api.mapper;

import com.scaledrop.sdupload.adapter.api.model.response.RegisterUploadResponse;
import com.scaledrop.sdupload.configuration.MapperConfiguration;
import com.scaledrop.sdupload.domain.upload.UploadObject;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface UploadResponseMapper {

  RegisterUploadResponse toResponse(UploadObject uploadObject);
}
