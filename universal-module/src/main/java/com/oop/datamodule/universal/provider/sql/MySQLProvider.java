package com.oop.datamodule.universal.provider.sql;

import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.mysql.MySQLDatabase;
import com.oop.datamodule.universal.Linker;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import com.oop.datamodule.universal.provider.StorageProvider;

public class MySQLProvider implements StorageProvider<MySQLDatabase> {
  @Override
  public <B extends UniversalBodyModel> Storage<B> provide(
      Linker<B> linker, MySQLDatabase database) {
    return new SQLStorageImpl<>(database, linker);
  }
}
