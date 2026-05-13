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

package com.scaledrop.sddownload.adapter

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.scaledrop.sddownload.WiremockTestBase

class ExampleControllerTest extends WiremockTestBase {

  private static final String EXAMPLE_ENDPOINT = "/api/v1/example"

  def 'should return 200'() {
    expect:
    mockMvc.perform(get(EXAMPLE_ENDPOINT)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
  }
}
