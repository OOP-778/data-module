package com.oop.datamodule.universal;

import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class UniversalStorage<T extends UniversalBodyModel> extends Storage<T> {

    @Getter
    private Storage<T> currentImplementation;

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
        this.currentImplementation = storage;
        return this;
    }

    @Override
    public void save(T object, boolean async, Runnable callback) {
        currentImplementation.save(object, async, callback);
    }

    @Override
    public void load(boolean async, Runnable callback) {
        currentImplementation.load(async, callback);
    }

    @Override
    public void save(boolean async, Runnable callback) {
        currentImplementation.save(async, callback);
    }

    @Override
    public void remove(T object) {
        currentImplementation.remove(object);
    }

    @Override
    public List<Consumer<Storage<T>>> getOnLoad() {
        return super.getOnLoad();
    }
}
