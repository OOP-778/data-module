package com.oop.datamodule;

import com.google.common.collect.Sets;
import com.oop.datamodule.body.DataBody;
import com.oop.datamodule.storage.Storage;
import com.oop.datamodule.util.Loadable;
import com.oop.datamodule.util.Saveable;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;
import java.util.Set;

public class StorageHolder implements Loadable, Saveable {

    @Getter
    private final Set<Storage<?>> storages = Sets.newConcurrentHashSet();

    public void registerStorage(@NonNull Storage storage) {
        storages.add(storage);
    }

    public <T extends DataBody> Optional<Storage<T>> getStorageOf(Class<T> clazz) {
        return storages
                .stream()
                .filter(storage -> storage.accepts(clazz))
                .map(storage -> (Storage<T>) storage)
                .findFirst();
    }

    public <T extends Storage> T getStorage(Class<T> clazz) {
        return (T) storages.stream().filter(storage -> storage.getClass() == clazz).findFirst().orElse(null);
    }

    @Override
    public void save(boolean async, Runnable callback) {
        storages.forEach(storage -> storage.save(async, callback));
    }

    @Override
    public void load(boolean async, Runnable callback) {
        storages.forEach(storage -> storage.load(async, callback));
    }
}
