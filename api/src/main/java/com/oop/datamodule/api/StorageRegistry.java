package com.oop.datamodule.api;

import com.oop.datamodule.api.model.ModelBody;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.api.util.Loadable;
import com.oop.datamodule.api.util.Saveable;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;

public class StorageRegistry implements Loadable, Saveable {

  @Getter private final Map<Class, Storage<? extends ModelBody>> storages = new IdentityHashMap<>();

  public void registerStorage(@NonNull Storage<? extends ModelBody> storage) {
    storages.put(storage.getClass(), storage);
  }

  public <T extends ModelBody> Optional<Storage<T>> getStorageOf(Class<T> clazz) {
    return storages.values().stream()
        .filter(storage -> storage.accepts(clazz))
        .map(storage -> (Storage<T>) storage)
        .findFirst();
  }

  public <T extends Storage> T getStorage(Class<T> clazz) {
    return (T) storages.get(clazz);
  }

  @Override
  public void save(boolean async, Runnable callback) {
    storages.values().forEach(storage -> storage.save(async, callback));
  }

  @Override
  public void load(boolean async, Runnable callback) {
    storages.values().forEach(storage -> storage.load(async, callback));
  }

  public List<Storage> getStorageList() {
    return new LinkedList<>(storages.values());
  }

  public void shutdown() {
    for (Storage<? extends ModelBody> storage : storages.values()) {
      storage.shutdown();
    }
  }
}
