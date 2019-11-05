package com.solozobov.andrei.utils;

import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * solozobov on 05.11.2019
 */
public class CountingThreadFactory implements ThreadFactory {

  @VisibleForTesting final String name;
  @VisibleForTesting final AtomicLong threadCounter;

  public CountingThreadFactory(@NotNull String name) {
    this.name = name;
    this.threadCounter = new AtomicLong();
  }

  @Override
  public Thread newThread(@NotNull Runnable runnable) {
    final Thread newThread = new Thread(runnable, name + "__Thread_" + threadCounter.incrementAndGet());

    if(newThread.isDaemon()) {
      newThread.setDaemon(false);
    }
    return newThread;
  }
}
