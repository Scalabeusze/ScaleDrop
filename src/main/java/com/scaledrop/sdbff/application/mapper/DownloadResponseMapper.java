package com.scaledrop.sdbff.application.mapper;

import com.scaledrop.sdbff.adapter.api.model.download.response.FileAPIResponse;
import com.scaledrop.sdbff.adapter.api.model.download.response.FileDownloadAPIResponse;
import com.scaledrop.sdbff.configuration.MapperConfiguration;
import com.scaledrop.sdbff.domain.download.FileDownloadHistory;
import com.scaledrop.sdbff.domain.download.FileObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface DownloadResponseMapper {

  @Mapping(target = "eTag", source = "ETag")
  FileObject toDomain(FileAPIResponse response);

  FileDownloadHistory toDomain(FileDownloadAPIResponse response);
}
