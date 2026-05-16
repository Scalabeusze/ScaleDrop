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

package com.scaledrop.sddownload.adapter.db;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class OffsetBasedPageRequest implements Pageable {

  private final int limit;
  private final int offset;

  @Override
  public int getPageNumber() {
    if (limit == 0) {
      return 0;
    }
    return offset / limit;
  }

  @Override
  public int getPageSize() {
    return limit;
  }

  @Override
  public long getOffset() {
    return offset;
  }

  @Override
  public Sort getSort() {
    return Sort.unsorted();
  }

  @Override
  public Pageable next() {
    return new OffsetBasedPageRequest(limit, offset + limit);
  }

  @Override
  public Pageable previousOrFirst() {
    if (!hasPrevious()) {
      return first();
    }
    return new OffsetBasedPageRequest(limit, Math.max(offset - limit, 0));
  }

  @Override
  public Pageable first() {
    return new OffsetBasedPageRequest(limit, 0);
  }

  @Override
  public Pageable withPage(int pageNumber) {
    return new OffsetBasedPageRequest(limit, pageNumber * limit);
  }

  @Override
  public boolean hasPrevious() {
    return offset > 0;
  }
}
