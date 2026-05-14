/*
 * Copyright 2026-present Scalabeusze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.scaledrop.sddownload.adapter.api

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.scaledrop.sddownload.WiremockTestBase
import com.scaledrop.sddownload.utilities.Initializer
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client

class FileControllerTest extends WiremockTestBase {

  private static final String FILES_ENDPOINT = "/api/v1/files"

  @Autowired
  S3Client s3Client

  def "should list all files when prefix is not provided"() {
    given:
    putObject("exports/list/report.csv", "report")
    putObject("exports/list/archive.csv", "archive")
    putObject("exports/other/report.csv", "other")

    when:
    def result = mockMvc.perform(get(FILES_ENDPOINT)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.key as Set == [
      "exports/list/report.csv",
      "exports/list/archive.csv",
      "exports/other/report.csv"
    ] as Set
    response.every { it.size != null }
    response.every { it.lastModified != null }
    response.every { it.eTag != null }
  }

  def "should list files by optional prefix"() {
    given:
    putObject("exports/prefix/report.csv", "report")
    putObject("exports/prefix/archive.csv", "archive")
    putObject("exports/other/report.csv", "other")

    when:
    def result = mockMvc.perform(get(FILES_ENDPOINT)
        .param("prefix", "exports/prefix/")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.key as Set == [
      "exports/prefix/report.csv",
      "exports/prefix/archive.csv"
    ] as Set
  }

  def "should handle s3 pagination internally"() {
    given:
    (1..1005).each {
      putObject(String.format("exports/page/file-%04d.txt", it), it.toString())
    }

    when:
    def result = mockMvc.perform(get(FILES_ENDPOINT)
        .param("prefix", "exports/page/")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.size() == 1005
    response*.key.contains("exports/page/file-0001.txt")
    response*.key.contains("exports/page/file-1005.txt")
  }

  private void putObject(String key, String content) {
    s3Client.putObject({
      it.bucket(Initializer.FILESERVER_BUCKET_NAME).key(key)
    }, RequestBody.fromString(content))
  }

  private static Object parseJson(String json) {
    new JsonSlurper().parseText(json)
  }
}
