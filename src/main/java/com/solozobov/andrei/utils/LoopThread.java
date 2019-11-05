package com.solozobov.andrei.utils;

import com.solozobov.andrei.RememberException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * solozobov on 23.07.2019
 */
@SuppressWarnings("WeakerAccess")
public class LoopThread {
  private static final Logger LOG = LoggerFactory.getLogger(LoopThread.class);

  private final Thread loopThread;
  private final Supplier<Long> delayFunction;

  private Runnable beforeLoopAction;
  private final Runnable inLoopAction;

  private final AtomicBoolean pausing = new AtomicBoolean(false);
  private final AtomicBoolean paused = new AtomicBoolean(false);
  private final AtomicBoolean terminating = new AtomicBoolean(false);
  private final AtomicBoolean successfullyTerminated = new AtomicBoolean(false);
  private Consumer<Long> loopDurationMillisConsumer;

  public LoopThread(@NotNull String threadName, @NotNull Runnable loopAction, long loopDelayMillis) {
    this(threadName, loopAction, () -> loopDelayMillis);
  }

  public LoopThread(@NotNull String threadName, @NotNull Runnable loopAction, @NotNull Supplier<Long> loopDelayMillis) {
    this.inLoopAction = loopAction;
    this.delayFunction = loopDelayMillis;

    this.loopThread = new Thread(this::run, "LoopThread__" + threadName);
    loopThread.setDaemon(true);
    loopThread.setUncaughtExceptionHandler((thread, throwable)
        -> LOG.error("Loop thread '" + loopThread.getName() + "' failed", throwable));
  }

  public LoopThread beforeLoop(@NotNull Runnable beforeLoopAction) {
    this.beforeLoopAction = beforeLoopAction;
    return this;
  }

  public LoopThread exportLoopDuration(@NotNull Consumer<Long> loopDurationMillisConsumer) {
    this.loopDurationMillisConsumer = loopDurationMillisConsumer;
    return this;
  }

  public LoopThread start() {
    if (loopThread.isAlive()) {
      throw new RememberException("Loop thread '%s' was already started", loopThread.getName());
    }
    loopThread.start();
    return this;
  }

  public void pause() {
    pausing.set(true);
  }

  public void resume() {
    pausing.set(false);
  }

  public void terminate() {
    terminating.set(true);
  }

  public boolean isSuccessfullyTerminated() {
    return successfullyTerminated.get();
  }

  private void run() {
    if (beforeLoopAction != null) {
      beforeLoopAction.run();
    }

    while(!terminating.get()) {
      if (pausing.get()) {
        if (paused.compareAndSet(false, true)) {
          LOG.info("Loop thread '{}' is now on pause", loopThread.getName());
        }
      } else {
        if (paused.compareAndSet(true, false)) {
          LOG.info("Loop thread '{}' is now running", loopThread.getName());
        }

        try {
          final long startMillis = System.currentTimeMillis();
          inLoopAction.run();
          if (loopDurationMillisConsumer != null) {
            loopDurationMillisConsumer.accept(System.currentTimeMillis() - startMillis);
          }
        }
        catch(Exception e) {
          LOG.warn("Loop thread '" + loopThread.getName() + "' loop failed with exception", e);
        }
      }

      try {
        Thread.sleep(delayFunction.get());
      } catch (InterruptedException e) {
        LOG.warn("Loop thread '{}' interrupted", loopThread.getName());
        terminate();
      }
    }

    LOG.info("Loop thread '{}' finished", loopThread.getName());
    successfullyTerminated.set(true);
  }
}
