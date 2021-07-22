package com.oop.datamodule.implementation;

import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.database.DatabaseHandler;
import com.oop.datamodule.api.database.DatabaseStructure;
import com.oop.datamodule.api.PropertyHolder;
import com.oop.datamodule.api.PropertyStorage;
import com.oop.datamodule.api.runner.RunnerContext;
import com.oop.datamodule.api.util.DataUtil;
import com.oop.datamodule.implementation.util.Helper;
import com.oop.datamodule.store.Store;
import com.oop.datamodule.store.memory.MemoryStore;
import com.oop.datamodule.store.query.Query;
import lombok.NonNull;

import java.util.*;

public abstract class ImplPropertyStorage implements PropertyStorage {
  protected final Map<String, MemoryStore<PropertyHolder>> modelsStore =
      new TreeMap<>(String::compareToIgnoreCase);
  protected final Map<String, DatabaseStructure> modelsStructures =
      new TreeMap<>(String::compareToIgnoreCase);
  protected DatabaseHandler databaseHandler;
  protected String id;

  @Override
  public Optional<Store<PropertyHolder>> dataOf(String modelName) {
    return Optional.ofNullable(modelsStore.get(modelName).unmodifiableStore());
  }

  @Override
  public Set<String> models() {
    return Collections.unmodifiableSet(modelsStore.keySet());
  }

  @Override
  public Optional<Collection<PropertyHolder>> valuesOf(@NonNull String modelName) {
    return Optional.ofNullable(modelsStore.get(modelName).unmodifiableStore());
  }

  @Override
  public Optional<DatabaseHandler> databaseHandler() {
    return Optional.ofNullable(databaseHandler);
  }

  @Override
  public void databaseHandler(DatabaseHandler databaseHandler) {
    this.databaseHandler = databaseHandler;
  }

  @Override
  public void removeObject(String modelName, Object... args) {
    final MemoryStore<PropertyHolder> modelObjects = modelsStore.get(modelName);

    final Map<String, Object> keys = Helper.mapFromArray(args);
    for (Map.Entry<String, Object> keyEntry : keys.entrySet()) {
      List<PropertyHolder> remove =
          modelObjects.remove(Query.where(keyEntry.getKey(), keyEntry.getValue()), -1);
      for (PropertyHolder propertyHolder : remove) {}
    }
  }

  @Override
  public void remove(PropertyHolder holder) {
    String holderId = holder.modelId();
    MemoryStore<PropertyHolder> holderStore = modelsStore.get(holderId);
    if (holderStore == null) {
      return;
    }

    holderStore.remove(holder);
  }

  protected void callDbRemove(
      @NonNull String modelId, @NonNull String identifier, @NonNull Object value) {
    RunnerContext.asyncRunner()
        .accept(
            String.format("db-remove-%s-%s-%s", modelId, identifier, value),
            () -> {
              databaseHandler.remove(
                  modelsStructures.get(modelId),
                  DatabaseHandler.ObjectIdentifier.builder()
                      .keyIdentifier(identifier)
                      .objectThatIdentifies(new SerializedData(DataUtil.wrap(value)))
                      .build());
            });
  }

  protected void newModel() {}

  @Override
  public String id() {
    return id;
  }

  protected static class ModelStruct {}
}
