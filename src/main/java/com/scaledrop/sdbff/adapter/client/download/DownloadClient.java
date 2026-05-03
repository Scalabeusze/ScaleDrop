package com.scaledrop.sdbff.adapter.client.download;

import com.scaledrop.sdbff.adapter.client.download.configuration.DownloadClientConfiguration;
import com.scaledrop.sdbff.configuration.feign.ScaleDropFeignConfiguration;
import com.scaledrop.sdbff.domain.download.DownloadObject;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "download-service-client",
    url = "${download-service.url}",
    configuration = {ScaleDropFeignConfiguration.class, DownloadClientConfiguration.class})
public interface DownloadClient {

  String DOWNLOAD_URL = "/internal/api/v1/download";

  @GetMapping(DOWNLOAD_URL + "/{fileId}")
  DownloadObject getDownloadObject(@PathVariable("fileId") UUID fileId);
}
