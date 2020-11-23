package com.oop.datamodule.universal;

import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class UniversalStorage<T extends UniversalBodyModel> extends Storage<T> {
    private Storage<T> storageImpl;

    @Getter (value = AccessLevel.PACKAGE)
    private final Consumer<T> adder = this::onAdd;

    @Getter (value = AccessLevel.PACKAGE)
    private final Consumer<T> remover = this::onRemove;

    @Getter (value = AccessLevel.PACKAGE)
    private final Supplier<Map<String, Class<T>>> variantsSupplier = this::getVariants;

    @Getter
    private final Linker<T> linker = new Linker<>(this);

    public UniversalStorage(
            @NonNull StorageRegistry storageRegistry
    ) {
        super(storageRegistry);
    }

    public UniversalStorage<T> currentImplementation(Storage<T> storage) {
        this.storageImpl = storage;
        return this;
    }

    @Override
    public void save(T object, boolean async, Runnable callback) {
        storageImpl.save(object, async, callback);
    }

    @Override
    public void load(boolean async, Runnable callback) {
        storageImpl.load(async, callback);
    }

    @Override
    public void save(boolean async, Runnable callback) {
        storageImpl.save(async, callback);
    }

    @Override
    public void remove(T object) {
        storageImpl.remove(object);
    }
}
