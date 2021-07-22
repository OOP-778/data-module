package com.oop.datamodule.property.implementation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.oop.datamodule.property.api.runner.RunnerContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class PropertyController {

  private static PropertyController INSTANCE;
  @Getter private final ImplStorageRegistry registry;
  @Getter private final Gson gson;

  @Getter
  private Consumer<Throwable> errorHandler =
      (error) -> {
        System.out.println("====== ERROR HAPPENED IN DB CONTEXT ======");
        printError(error);
        System.out.println("======  ======");
      };

  protected PropertyController(Builder builder) {
    if (INSTANCE != null) {
      throw new IllegalStateException("Property Controller is already initialized!");
    }

    if (builder.errorHandler != null) {
      this.errorHandler = builder.errorHandler;
    }

    if (builder.registry != null) {
      this.registry = builder.registry;
    } else {
      this.registry = new ImplStorageRegistry();
    }

    final GsonBuilder gsonBuilder = new GsonBuilder();
    for (TypeAdapterInfo<?> typeAdapterInfo : builder.adapterList) {
      if (typeAdapterInfo.hierarchy) {
        gsonBuilder.registerTypeHierarchyAdapter(
            typeAdapterInfo.getType(), typeAdapterInfo.getAdapter());
        continue;
      }

      gsonBuilder.registerTypeAdapter(typeAdapterInfo.getType(), typeAdapterInfo.getAdapter());
    }
    this.gson = gsonBuilder.create();

    createAsyncRunner(builder.asyncRunnerThreads);
    INSTANCE = this;
  }

  public static PropertyController create() {
    return create(null);
  }

  public static PropertyController create(Consumer<Builder> builderConsumer) {
    final Builder builder = new Builder();
    if (builderConsumer != null) {
      builderConsumer.accept(builder);
    }

    return new PropertyController(builder);
  }

  public static PropertyController getInstance() {
    return INSTANCE;
  }

  protected void printError(Throwable throwable) {
    System.out.println("-" + throwable.getMessage());
    for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
      System.out.println(stackTraceElement.toString());
    }

    if (throwable.getCause() != null) {
      printError(throwable.getCause());
    }
  }

  protected void createAsyncRunner(int threads) {
    final ForkJoinPool joinPool = new ForkJoinPool(threads);
    new RunnerContext("async-runner") {
      @Override
      protected void internalAccept(Runnable runnable) {
        joinPool.submit(runnable);
      }

      @Override
      public void shutdown() {
        joinPool.shutdown();
        try {
          joinPool.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          throw new IllegalStateException("Failed to shutdown async task runner TIMEOUT!");
        }
      }
    };
  }

  public void shutdown() {
    // Shutdown our runners
    for (RunnerContext runnerContext : RunnerContext.available()) {
      runnerContext.shutdown();
    }

    // Shutdown storages
    registry.shutdown();
  }

  protected synchronized void handleError(Thread thread, Throwable throwable) {
    IllegalStateException error =
        new IllegalStateException("Error happened in thread " + thread.getName());
    error.setStackTrace(throwable.getStackTrace());
    this.errorHandler.accept(error);
  }

  @Accessors(fluent = true, chain = true)
  public static class Builder {
    private final List<TypeAdapterInfo<?>> adapterList = new LinkedList<>();
    @Setter private Consumer<Throwable> errorHandler;
    @Setter private ImplStorageRegistry registry;
    @Setter private int asyncRunnerThreads = 2;

    public <T> Builder addAdapter(@NonNull TypeAdapterInfo<T> typeAdapterInfo) {
      this.adapterList.add(typeAdapterInfo);
      return this;
    }
  }

  @AllArgsConstructor
  @Getter
  public static class TypeAdapterInfo<T> {

    private Class<T> type;
    private boolean hierarchy;
    private TypeAdapter<T> adapter;
  }
}
