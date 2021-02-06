package com.oop.datamodule.universal.provider.sql;

import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.h2.H2Database;
import com.oop.datamodule.universal.Linker;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import com.oop.datamodule.universal.provider.StorageProvider;

public class H2Provider implements StorageProvider<H2Database> {

    @Override
    public <B extends UniversalBodyModel> Storage<B> provide(Linker<B> linker,
        H2Database settings) {
        return new SQLStorageImpl<>(settings, linker);
    }
}
