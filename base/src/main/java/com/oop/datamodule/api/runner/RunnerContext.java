package com.oop.datamodule.api.runner;

import com.oop.datamodule.implementation.PropertyController;

import java.util.*;

public abstract class RunnerContext {

  private static final Map<String, RunnerContext> REGISTERED_RUNNERS =
      new TreeMap<>(String::compareToIgnoreCase);

  static {
    new RunnerContext("current-thread-runner") {
      @Override
      protected void internalAccept(Runnable runnable) {
        runnable.run();
      }

      @Override
      public void shutdown() {
        // Do nothing
      }
    };
  }

  private final String identifier;

  public RunnerContext(String identifier) {
    REGISTERED_RUNNERS.put(identifier, this);
    this.identifier = identifier;
  }

  public static RunnerContext blockingRunner() {
    return valueOf("current-thread-runner")
        .orElseThrow(() -> new IllegalStateException("Default Task Runner is not initialized?!"));
  }

  public static RunnerContext asyncRunner() {
    return valueOf("async-runner")
            .orElseThrow(() -> new IllegalStateException("async task runner wasn't registered!"));
  }

  public static Optional<RunnerContext> valueOf(String identifier) {
    return Optional.ofNullable(REGISTERED_RUNNERS.get(identifier));
  }

  public static Collection<RunnerContext> available() {
    return REGISTERED_RUNNERS.values();
  }

  public String identifier() {
    return identifier;
  }

  public void accept(String task, Runnable runnable) {
    internalAccept(
        () -> {
          // Generate UUID of the task
          UUID taskUUID = UUID.randomUUID();
          try {
            markStart(taskUUID, task);
            runnable.run();
          } catch (Throwable throwable) {
            IllegalStateException error =
                new IllegalStateException(
                    String.format(
                        "While running a database task: %s, on runner: %s error happened",
                        task, identifier),
                    throwable);

            PropertyController.getInstance().getErrorHandler().accept(error);
          } finally {
            markEnd(taskUUID, task);
          }
        });
  }

  /**
   * Mark the start of a task. Can be overridden for profiling purposes
   *
   * @param taskUUID UUID of the task
   * @param taskName the name of the task
   */
  protected void markStart(UUID taskUUID, String taskName) {}

  /**
   * Mark the end of a task. Can be overridden for profiling purposes
   *
   * @param taskUUID UUID of the task
   * @param taskName the name of the task
   */
  protected void markEnd(UUID taskUUID, String taskName) {}

  /**
   * To handle errors, profiling, etc. we call this on {@link RunnerContext#accept(String task,
   * Runnable runnable)}
   */
  protected abstract void internalAccept(Runnable runnable);

  /**
   * Called on shutdown
   */
  public abstract void shutdown();
}
