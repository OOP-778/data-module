package com.oop.datamodule.api;

import com.oop.datamodule.api.model.ModelBody;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.api.util.Loadable;
import com.oop.datamodule.api.util.Saveable;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StorageRegistry implements Loadable, Saveable {

  @Getter private final Set<Storage<? extends ModelBody>> storages = ConcurrentHashMap.newKeySet();

  public void registerStorage(@NonNull Storage<? extends ModelBody> storage) {
    storages.add(storage);
  }

  public <T extends ModelBody> Optional<Storage<T>> getStorageOf(Class<T> clazz) {
    return storages.stream()
        .filter(storage -> storage.accepts(clazz))
        .map(storage -> (Storage<T>) storage)
        .findFirst();
  }

  public <T extends Storage> T getStorage(Class<T> clazz) {
    return (T)
        storages.stream().filter(storage -> storage.getClass() == clazz).findFirst().orElse(null);
  }

  @Override
  public void save(boolean async, Runnable callback) {
    storages.forEach(storage -> storage.save(async, callback));
  }

  @Override
  public void load(boolean async, Runnable callback) {
    storages.forEach(storage -> storage.load(async, callback));
  }

  public void shutdown() {
    for (Storage<? extends ModelBody> storage : storages) {
      storage.shutdown();
    }
  }
}
