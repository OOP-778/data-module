package com.oop.datamodule.property.api;

import com.oop.datamodule.property.api.general.Saveable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Holder of all possible property storages */
public interface StorageRegistry extends Saveable {

  /** Unmodifiable storage map */
  Map<String, PropertyStorage> storages();

  /** Assign storage to the registry */
  void assignStorage(@NonNull String storageId, @NonNull PropertyStorage storage);

  /** Remove storage from the registry */
  void removeStorage(@NonNull String storageId);

  default Optional<PropertyStorage> getStorage(@NonNull String storageId) {
    return Optional.ofNullable(storages().get(storageId));
  }
}
