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

package com.scaledrop.sdupload.configuration.transactionoutbox;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

public class SemaphoreBoundedExecutor implements Executor {

  private final ExecutorService executor;
  private final Semaphore semaphore;

  public SemaphoreBoundedExecutor(ExecutorService executor, int maxConcurrentTasks) {
    this.executor = executor;
    this.semaphore = new Semaphore(maxConcurrentTasks);
  }

  @Override
  public void execute(Runnable command) {
    try {
      semaphore.acquire();
      executor.execute(
          () -> {
            try {
              command.run();
            } finally {
              semaphore.release();
            }
          });
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Task execution was interrupted", e);
    }
  }

  public void shutdown() {
    executor.shutdown();
  }
}
