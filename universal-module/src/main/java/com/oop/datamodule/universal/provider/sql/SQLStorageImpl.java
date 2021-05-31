package com.oop.datamodule.universal.provider.sql;

import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.commonsql.database.SQLDatabase;
import com.oop.datamodule.commonsql.storage.SqlStorage;
import com.oop.datamodule.universal.Linker;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import lombok.NonNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SQLStorageImpl<T extends UniversalBodyModel> extends SqlStorage<T> {
  private final Linker<T> linker;

  public SQLStorageImpl(@NonNull SQLDatabase database, @NonNull Linker<T> linker) {
    super(database);
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
}
