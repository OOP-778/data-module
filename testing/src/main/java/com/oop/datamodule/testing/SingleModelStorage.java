package com.oop.datamodule.testing;

import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.universal.UniversalStorage;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

public abstract class SingleModelStorage extends UniversalStorage<UniversalBodyModel> {

  protected String key;
  protected UniversalBodyModel model;

  public SingleModelStorage(@NonNull StorageRegistry storageRegistry, String named) {
    super(storageRegistry);

    this.key = named;
    this.model = createModel();
  }

  protected abstract String[] getStructure();

  protected abstract void serialize(SerializedData serializedData);

  protected abstract void deserialize(SerializedData serializedData);

  @Override
  public <B extends UniversalBodyModel> B construct(Class<B> clazz) {
    return createModel();
  }

  @Override
  public String findVariantNameFor(Class<?> clazz) {
    return key;
  }

  protected <T extends UniversalBodyModel> T createModel() {
    final SingleModelStorage storage = this;

    return (T)
        new UniversalBodyModel() {
          @Override
          public void save(boolean b, Runnable runnable) {
            storage.save(this, b, runnable);
          }

          @Override
          public void serialize(SerializedData serializedData) {
            serializedData.write("name", storage.key);
            storage.serialize(serializedData);
          }

          @Override
          public void deserialize(SerializedData serializedData) {
            storage.deserialize(serializedData);
          }

          @Override
          public String getKey() {
            return storage.key;
          }

          @Override
          public String getIdentifierKey() {
            return "name";
          }

          @Override
          public String[] getStructure() {
            return Stream.concat(
                    Arrays.stream(new String[] {"name"}), Arrays.stream(storage.getStructure()))
                .toArray(String[]::new);
          }
        };
  }

  @Override
  protected void onAdd(UniversalBodyModel universalBodyModel) {}

  @Override
  protected void onRemove(UniversalBodyModel universalBodyModel) {}

  @Override
  public Stream<UniversalBodyModel> stream() {
    return Stream.of(model);
  }

  @Override
  public Iterator<UniversalBodyModel> iterator() {
    return Collections.singletonList(model).iterator();
  }
}
