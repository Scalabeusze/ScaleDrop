package com.scaledrop.sdbff.adapter.client.download;

import com.scaledrop.sdbff.adapter.api.model.download.response.FileAPIResponse;
import com.scaledrop.sdbff.adapter.api.model.download.response.FileDownloadAPIResponse;
import com.scaledrop.sdbff.adapter.client.download.configuration.DownloadClientConfiguration;
import feign.Response;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "download-service-client",
    url = "${download-service.url}",
    configuration = {DownloadClientConfiguration.class})
public interface DownloadClient {

  String FILES_URL = "/api/v1/files";
  String DOWNLOADS_URL = "/api/v1/file-downloads";

  @GetMapping(FILES_URL)
  List<FileAPIResponse> listFiles(
      @RequestParam(value = "prefix", required = false) String prefix,
      @RequestParam(value = "ownerId", required = false) UUID ownerId,
      @RequestParam(value = "limit", required = false, defaultValue = "1000") Integer limit,
      @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset);

  @GetMapping(FILES_URL + "/{fileId}")
  FileAPIResponse getFile(@PathVariable("fileId") UUID fileId);

  @GetMapping(FILES_URL + "/{fileId}/download")
  Response getDownloadObjectResponse(@PathVariable("fileId") UUID fileId);

  @GetMapping(FILES_URL + "/sync")
  List<FileAPIResponse> syncFiles();

  @GetMapping(DOWNLOADS_URL)
  List<FileDownloadAPIResponse> listFileDownloads(
      @RequestParam(value = "fileId", required = false) UUID fileId,
      @RequestParam(value = "ownerId", required = false) UUID ownerId,
      @RequestParam(value = "limit", required = false, defaultValue = "1000") Integer limit,
      @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset);
}
