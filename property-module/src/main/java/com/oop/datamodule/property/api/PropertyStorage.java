package com.oop.datamodule.property.api;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/** This storage holds all the objects */
public interface PropertyStorage<T> {

  /** Stream of the all objects */
  default Stream<T> streamObjects() {
    return streamHolders().map(PropertyHolder::getWrappingObject);
  }

  /** Unmodifiable map of holders */
  Map<UUID, PropertyHolder<T>> holders();

  default UUID generateObjectUUID() {
    final Map<UUID, PropertyHolder<T>> holders = holders();

    UUID uuid;
    while (true) {
      uuid = UUID.randomUUID();
      if (holders.containsKey(uuid)) continue;

      return uuid;
    }
  }

  Stream<PropertyHolder<T>> streamHolders();
}
