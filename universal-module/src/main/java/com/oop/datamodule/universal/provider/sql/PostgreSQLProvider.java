package com.oop.datamodule.universal.provider.sql;

import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.postgresql.PostgreDatabase;
import com.oop.datamodule.universal.Linker;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import com.oop.datamodule.universal.provider.StorageProvider;

public class PostgreSQLProvider implements StorageProvider<PostgreDatabase> {
  @Override
  public <B extends UniversalBodyModel> Storage<B> provide(
      Linker<B> linker, PostgreDatabase database) {
    return new SQLStorageImpl<>(database, linker);
  }
}
