package com.oop.datamodule.universal.provider;

import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.universal.Linker;
import com.oop.datamodule.universal.model.UniversalBodyModel;

public interface StorageProvider<T> {
    <B extends UniversalBodyModel> Storage<B> provide(Linker<B> linker, T settings);
}
