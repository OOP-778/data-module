package com.oop.datamodule.property.implementation;

import com.oop.datamodule.api.util.Preconditions;
import com.oop.datamodule.property.api.PropertyStorage;
import com.oop.datamodule.property.api.StorageRegistry;
import lombok.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ImplStorageRegistry implements StorageRegistry {

  private final Map<String, PropertyStorage> storageMap =
      new TreeMap<>(String::compareToIgnoreCase);

  @Override
  public Map<String, PropertyStorage> storages() {
    return Collections.unmodifiableMap(storageMap);
  }

  @Override
  public void assignStorage(@NonNull String storageId, @NonNull PropertyStorage storage) {
    Preconditions.checkArgument(
        storage instanceof ImplPropertyStorage, "The storage must extend ImplPropertyStorage!");
    ((ImplPropertyStorage) storage).id = storageId;
    this.storageMap.put(storageId, storage);
  }

  @Override
  public void removeStorage(@NonNull String storageId) {
    storageMap.remove(storageId);
  }

  public void shutdown() {
    for (PropertyStorage storage : storageMap.values()) {
      // TODO: Add shutdown
    }
  }

  @Override
  public void save(SaveArgs saveArgs) {

  }
}
