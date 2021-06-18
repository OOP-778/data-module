package com.oop.datamodule.universal;

import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class UniversalStorage<T extends UniversalBodyModel> extends Storage<T> {

  @Getter(value = AccessLevel.PACKAGE)
  private final Consumer<T> adder = this::onAdd;

  @Getter(value = AccessLevel.PACKAGE)
  private final Consumer<T> remover = this::onRemove;

  @Getter(value = AccessLevel.PACKAGE)
  private final Supplier<Map<String, Class<T>>> variantsSupplier = this::getVariants;

  @Getter private final Linker<T> linker = new Linker<>(this);
  @Getter private Storage<T> currentImplementation;

  public UniversalStorage(@NonNull StorageRegistry storageRegistry) {
    super(storageRegistry);
  }

  @Override
  public void shutdown() {
    if (currentImplementation == null) return;
    currentImplementation.shutdown();
  }

  public UniversalStorage<T> currentImplementation(Storage<T> storage) {
    if (currentImplementation != null) currentImplementation.shutdown();

    this.currentImplementation = storage;
    return this;
  }

  protected void ensureImplementationIsFound() {
    Objects.requireNonNull(currentImplementation, "Current implementation is not found!");
  }

  @Override
  public void save(T object, boolean async, Runnable callback) {
    ensureImplementationIsFound();
    currentImplementation.save(object, async, callback);
  }

  @Override
  public void load(boolean async, Runnable callback) {
    ensureImplementationIsFound();
    currentImplementation.load(async, callback);
  }

  @Override
  public void save(boolean async, Runnable callback) {
    ensureImplementationIsFound();
    currentImplementation.save(async, callback);
  }

  @Override
  public void remove(T object) {
    ensureImplementationIsFound();
    currentImplementation.remove(object);
  }

  @Override
  protected void handleError(Throwable throwable) {
    super.handleError(throwable);
  }

  @Override
  public String findVariantNameFor(Class<?> clazz) {
    return super.findVariantNameFor(clazz);
  }

  @Override
  public List<Consumer<Storage<T>>> getOnLoad() {
    return super.getOnLoad();
  }
}
