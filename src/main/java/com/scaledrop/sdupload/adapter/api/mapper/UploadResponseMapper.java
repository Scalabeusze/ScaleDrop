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

package com.scaledrop.sdupload.adapter.api.mapper;

import com.scaledrop.sdupload.adapter.api.model.response.RegisterUploadResponse;
import com.scaledrop.sdupload.configuration.MapperConfiguration;
import com.scaledrop.sdupload.domain.upload.UploadObject;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface UploadResponseMapper {

  RegisterUploadResponse toResponse(UploadObject uploadObject);
}
