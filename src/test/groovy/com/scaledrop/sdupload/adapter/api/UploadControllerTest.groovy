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

package com.scaledrop.sdupload.adapter.api

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.fasterxml.jackson.databind.ObjectMapper
import com.scaledrop.sdupload.IntegrationTestBase
import com.scaledrop.sdupload.adapter.db.FileRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc

@AutoConfigureMockMvc
@WithMockUser(username = "scaledrop", roles = ["USER", "ADMIN", "INTERNAL"])
class UploadControllerTest extends IntegrationTestBase {

  @Autowired
  MockMvc mockMvc

  @Autowired
  ObjectMapper objectMapper

  @Autowired
  FileRepository fileRepository

  def "should successfully register a file, return S3 link and confirm upload"() {
    given: "A valid upload request for a file"
    def ownerId = UUID.randomUUID()
    def requestBody = [
      name       : "test-photo.png",
      contentType: "image/png",
      type       : "FILE",
      size       : 2048L,
      location   : "/"
    ]

    when: "Sending POST to /api/v1/upload to register the file"
    def registerResult = mockMvc.perform(post("/api/v1/upload")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", ownerId.toString())
        .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isCreated())
        .andReturn()

    then: "Response contains file ID, PENDING status and generated AWS S3 presigned URL"
    def responseBody = objectMapper.readValue(registerResult.response.contentAsString, Map.class)
    responseBody.fileId != null
    responseBody.status == "PENDING"
    responseBody.type == "FILE"
    responseBody.uploadUrl != null
    responseBody.uploadUrl.contains(ownerId.toString())

    and: "File is saved in the database with PENDING status"
    def fileId = UUID.fromString(responseBody.fileId as String)
    def savedFile = fileRepository.findById(fileId).orElse(null)
    savedFile != null
    savedFile.status == "PENDING"

    when: "Sending POST to confirm the physical upload"
    mockMvc.perform(post("/api/v1/upload/${fileId}/confirm")
        .with(csrf())
        .header("X-User-Id", ownerId.toString()))
        .andExpect(status().isOk())

    then: "File status in the database is updated to UPLOADED"
    def confirmedFile = fileRepository.findById(fileId).orElse(null)
    confirmedFile.status == "UPLOADED"
  }

  def "should successfully register a folder and set status to UPLOADED immediately"() {
    given: "A valid upload request for a folder"
    def ownerId = UUID.randomUUID()
    def requestBody = [
      name       : "vacation-photos",
      type       : "FOLDER",
      size       : 0L,
      location   : "/"
    ]

    when: "Sending POST to register the folder"
    def response = mockMvc.perform(post("/api/v1/upload")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", ownerId.toString())
        .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isCreated())
        .andReturn()

    then: "Upload URL is null and status is already UPLOADED"
    def responseBody = objectMapper.readValue(response.response.contentAsString, Map.class)
    responseBody.uploadUrl == null
    responseBody.status == "UPLOADED"
    responseBody.type == "FOLDER"
  }

  def "should successfully register a file and confirm upload with timing"() {
    given: "A valid upload request"
    def ownerId = UUID.randomUUID()
    def requestBody = [name: "test.png", type: "FILE", size: 1024L, location: "/"]

    when: "Registering the file"
    long startRegister = System.currentTimeMillis()

    def registerResult = mockMvc.perform(post("/api/v1/upload")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-User-Id", ownerId.toString())
        .content(objectMapper.writeValueAsString(requestBody)))
        .andReturn()

    long endRegister = System.currentTimeMillis()
    long registerDuration = endRegister - startRegister

    then: "Registration is fast"
    registerDuration < 500

    when: "Confirming upload"
    def fileId = objectMapper.readValue(registerResult.response.contentAsString, Map.class).fileId

    long startConfirm = System.currentTimeMillis()

    mockMvc.perform(post("/api/v1/upload/${fileId}/confirm")
        .with(csrf())
        .header("X-User-Id", ownerId.toString()))
        .andExpect(status().isOk())

    long endConfirm = System.currentTimeMillis()
    long confirmDuration = endConfirm - startConfirm

    then: "Confirmation is also fast"
    confirmDuration < 500

    println "Performance Report: Registration: ${registerDuration}ms, Confirmation: ${confirmDuration}ms"
  }
}