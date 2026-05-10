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
