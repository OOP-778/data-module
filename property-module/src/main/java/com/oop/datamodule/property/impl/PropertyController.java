package com.oop.datamodule.property.impl;

import com.oop.datamodule.property.impl.key.ImplKeyRegistry;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyController {

  private static PropertyController INSTANCE;

  @Getter
  private final ImplKeyRegistry baseKeyRegistry = new ImplKeyRegistry(null);

  private final ForkJoinPool DB_EXECUTOR;

  @Setter
  private Consumer<Throwable> errorHandler = (error) -> {
    System.out.println("====== ERROR HAPPENED IN DB CONTEXT ======");
    printError(error);
    System.out.println("======  ======");
  };

  protected void printError(Throwable throwable) {
    System.out.println("-" + throwable.getMessage());
    for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
      System.out.println(stackTraceElement.toString());
    }

    if (throwable.getCause() != null) {
      printError(throwable.getCause());
    }
  }

  public PropertyController(int threads) {
    if (INSTANCE != null) {
      throw new IllegalStateException("Property Controller is already initialized!");
    }

    INSTANCE = this;
    this.DB_EXECUTOR = new ForkJoinPool(threads, ForkJoinPool.defaultForkJoinWorkerThreadFactory, this::handleError, true);
  }

  protected synchronized void handleError(Thread thread, Throwable throwable) {
    IllegalStateException error = new IllegalStateException("Error happened in thread " + thread.getName());
    error.setStackTrace(throwable.getStackTrace());
    this.errorHandler.accept(error);
  }

  public void execute(Runnable runnable) {
    DB_EXECUTOR.submit(() -> {
      try {
        runnable.run();
      } catch (Throwable throwable) {
        handleError(Thread.currentThread(), throwable);
      }
    });
  }

  public static PropertyController getInstance() {
    return INSTANCE;
  }
}
