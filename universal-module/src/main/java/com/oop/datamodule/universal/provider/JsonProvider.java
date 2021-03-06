package com.oop.datamodule.universal.provider;

import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.json.storage.JsonStorage;
import com.oop.datamodule.universal.Linker;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import lombok.NonNull;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class JsonProvider implements StorageProvider<File> {
  @Override
  public <B extends UniversalBodyModel> Storage<B> provide(Linker<B> linker, File directory) {
    return new JsonStorageImpl<>(linker, directory);
  }

  protected static class JsonStorageImpl<T extends UniversalBodyModel> extends JsonStorage<T> {
    private final Linker<T> linker;

    public JsonStorageImpl(@NonNull Linker<T> linker, @NonNull File directory) {
      super(directory);
      this.linker = linker;
    }

    @Override
    protected void onAdd(T object) {
      linker.onAdd(object);
    }

    @Override
    protected void onRemove(T object) {
      linker.onRemove(object);
    }

    @Override
    public Stream<T> stream() {
      return linker.getStorage().stream();
    }

    @Override
    public Iterator<T> iterator() {
      return linker.getStorage().iterator();
    }

    @Override
    public Map<String, Class<T>> getVariants() {
      return linker.getVariants();
    }

    @Override
    protected List<Consumer<Storage<T>>> getOnLoad() {
      return linker.getOnLoad();
    }

    @Override
    public <B extends T> B construct(Class<B> clazz) {
      return linker.construct(clazz);
    }

    @Override
    public String findVariantNameFor(Class<?> clazz) {
      return linker.findVariantNameFor(clazz);
    }

    @Override
    protected void handleError(Throwable throwable) {
      linker.handleError(throwable);
    }
  }
}
