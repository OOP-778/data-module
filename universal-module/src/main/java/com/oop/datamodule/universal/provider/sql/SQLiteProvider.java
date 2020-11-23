package com.oop.datamodule.universal.provider.sql;

import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.sqlite.SQLiteCredential;
import com.oop.datamodule.universal.Linker;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import com.oop.datamodule.universal.provider.StorageProvider;

public class SQLiteProvider implements StorageProvider<SQLiteCredential> {
    @Override
    public <B extends UniversalBodyModel> Storage<B> provide(Linker<B> linker, SQLiteCredential settings) {
        return new SQLStorageImpl<>(settings.build(), linker);
    }
}
