package com.oop.datamodule.implementation;

import com.google.gson.JsonElement;
import com.oop.datamodule.api.util.Preconditions;
import com.oop.datamodule.api.Property;
import com.oop.datamodule.api.PropertyHolder;
import com.oop.datamodule.api.holder.LoadedValue;
import com.oop.datamodule.api.runner.RunnerContext;
import com.oop.datamodule.api.util.DataUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class ImplProperty<T> implements Property<T> {

  private final String identifier;
  private final ImplPropertyHolder holder;
  private final Map<String, Object> settings;
  private final ImplLoadedValue loadedValue = new ImplLoadedValue();
  private final ValueLoader loader = new ValueLoader();

  private final AtomicBoolean loading = new AtomicBoolean(false);
  private final AtomicBoolean loaded = new AtomicBoolean(false);

  @Override
  public String id() {
    return identifier;
  }

  @Override
  public PropertyHolder holder() {
    return Objects.requireNonNull(holder, "Holder is not assigned yet!");
  }

  @Override
  public Map<String, Object> getSettings() {
    return Collections.unmodifiableMap(settings);
  }

  @Override
  public JsonElement serialize() {
    Preconditions.checkArgument(isLoaded(), "Cannot serialize unloaded value!");
    return DataUtil.wrap(loadedValue.get());
  }

  @Override
  public @NonNull Property<T> property() {
    return this;
  }

  @Override
  @SneakyThrows
  public LoadedValue<T> getBlocking(TimeUnit unit, long timeout) {
    if (isLoaded()) {
      return loadedValue;
    }

    return getAsync().get();
  }

  @Override
  public CompletableFuture<LoadedValue<T>> getAsync() {
    if (isLoaded()) {
      return CompletableFuture.completedFuture(loadedValue);
    }

    return loader.get().thenApply($ -> loadedValue);
  }

  @Override
  public boolean isLoaded() {
    return loaded.get();
  }

  public class ImplLoadedValue implements LoadedValue<T> {
    private final AtomicBoolean updated = new AtomicBoolean(false);
    private T value;

    @Override
    public boolean isUpdated() {
      return updated.get();
    }

    @Override
    public synchronized void set(T newValue) {}

    @Override
    public synchronized T get() {
      return value;
    }
  }

  public class ValueLoader {
    private CompletableFuture<T> future;

    public synchronized CompletableFuture<T> get() {
      if (future != null) return future;
      loading.set(true);

      future = new CompletableFuture<>();
      future.whenComplete(
          (object, error) -> {
            loading.set(false);
            future = null;

            if (error != null) {
              PropertyController.getInstance()
                  .handleError(
                      Thread.currentThread(),
                      new IllegalStateException("Failed to load value .. TODO", error));
              return;
            }

            loadedValue.value = object;
            loadedValue.updated.set(false);
          });

      RunnerContext.asyncRunner().accept("load-property-{model}-{objectId}-{identifier}", () -> {

      });

      return future;
    }
  }
}
