package com.oop.datamodule.storage;

import com.oop.datamodule.StorageHolder;
import com.oop.datamodule.body.FlatDataBody;

import java.util.Map;

public abstract class FileStorage<T extends FlatDataBody> extends Storage<T> {
    public FileStorage(StorageHolder storageHolder) {
        super(storageHolder);
    }

    public abstract Map<String, Class<? extends T>> getVariants();
}
