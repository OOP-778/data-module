package com.oop.datamodule.testing;

import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageRegistry;
import lombok.NonNull;

public class TEstSingleStorage extends SingleModelStorage{

    public TEstSingleStorage(@NonNull StorageRegistry storageRegistry) {
        super(storageRegistry, "test");
    }

    @Override
    protected String[] getStructure() {
        return new String[]{"hello"};
    }

    @Override
    protected void serialize(SerializedData serializedData) {
        serializedData.write("hello", "wfafaw");
    }

    @Override
    protected void deserialize(SerializedData serializedData) {

    }
}
