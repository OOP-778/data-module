package com.oop.datamodule.property.store.memory;

import com.oop.datamodule.property.store.AbstractStore;
import com.oop.datamodule.property.store.Store;
import com.oop.datamodule.property.store.identity.DefaultIdentityProvider;
import com.oop.datamodule.property.store.index.IndexManager;
import com.oop.datamodule.property.store.index.ReferenceIndexManager;
import com.oop.datamodule.property.store.reference.DefaultReferenceManager;
import com.oop.datamodule.property.store.reference.ReferenceManager;

import java.util.Collection;

/**
 * In memory implementation of a {@link Store}
 *
 * @param <V> type of item referenced
 */
public class MemoryStore<V> extends AbstractStore<V> {
  private MemoryStore(
      final ReferenceManager<V> referenceManager, final IndexManager<V> indexManager) {
    super(referenceManager, indexManager);
  }

  public MemoryStore() {
    this(
        new DefaultReferenceManager<>(
            new DefaultIdentityProvider(), new MemoryReferenceFactory<>()),
        new ReferenceIndexManager<>());
  }

  public MemoryStore(final Collection<V> items) {
    this();
    addAll(items);
  }

  @SafeVarargs
  public MemoryStore(final V... items) {
    this();
    addAll(items);
  }

  /**
   * New store builder
   *
   * @param <V> data type
   * @return builder
   */
  public static <V> MemoryStoreBuilder<V> newStore() {
    return new MemoryStoreBuilder<>();
  }

  /**
   * New store builder. This store isn't strongly typed, this is the same as calling
   * MemoryStore.&lt;String&gt;newStore()
   *
   * @param type data type
   * @param <V> data type
   * @return builder
   */
  public static <V> MemoryStoreBuilder<V> newStore(final Class<V> type) {
    return new MemoryStoreBuilder<>();
  }

  @Override
  protected Store<V> createCopy(
      final ReferenceManager<V> referenceManager, final IndexManager<V> indexManager) {
    return new MemoryStore<>(referenceManager.copy(), indexManager.copy());
  }

  @Override
  public String toString() {
    return getReferenceManager().getReferences().toString();
  }
}
