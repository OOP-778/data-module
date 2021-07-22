package com.oop.datamodule.property.store.memory;

import com.oop.datamodule.property.store.reference.Reference;
import com.oop.datamodule.property.store.reference.ReferenceFactory;

/**
 * Factory for creating in memory references
 *
 * @param <V> value type
 */
public class MemoryReferenceFactory<V> implements ReferenceFactory<V> {
  @Override
  public Reference<V> createReference(final V obj) {
    return new MemoryReference<>(obj);
  }
}
