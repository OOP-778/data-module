package com.oop.datamodule.universal.provider.sql;

import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.mysql.MySQLCredential;
import com.oop.datamodule.universal.Linker;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import com.oop.datamodule.universal.provider.StorageProvider;

public class MySQLProvider implements StorageProvider<MySQLCredential> {
    @Override
    public <B extends UniversalBodyModel> Storage<B> provide(Linker<B> linker, MySQLCredential settings) {
        return new SQLStorageImpl<>(settings.build(), linker);
    }
}
