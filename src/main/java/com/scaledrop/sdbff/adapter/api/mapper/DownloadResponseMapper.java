package com.scaledrop.sdbff.adapter.api.mapper;

import com.scaledrop.sdbff.adapter.api.model.response.DownloadAPIResponse;
import com.scaledrop.sdbff.configuration.MapperConfiguration;
import com.scaledrop.sdbff.domain.download.DownloadObject;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface DownloadResponseMapper {

  DownloadAPIResponse toResponse(DownloadObject downloadObject);
}