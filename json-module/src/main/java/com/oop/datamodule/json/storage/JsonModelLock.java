package com.oop.datamodule.json.storage;

import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.model.ModelBody;
import com.oop.datamodule.api.storage.lock.ModelLock;
import lombok.Getter;
import lombok.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class JsonModelLock<T extends ModelBody> extends ModelLock<T> {

    @Getter
    private File file;

    private JsonStorage<T> storage;

    public JsonModelLock(@NonNull T object, File file, JsonStorage<T> storage) {
        super(object);
        this.file = file;
        this.storage = storage;
    }

    public void save() {
        if (isLocked()) return;

        lockAndUse(() -> {
            try {
                if (!file.exists())
                    file.createNewFile();

                SerializedData data = new SerializedData();
                object.serialize(data);

                if (!storage.isObjectUpdated(object.getKey(), data)) return;

                data.write(storage.getTypeVar(), storage.findVariantNameFor(object.getClass()));

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
                JsonStorage.getPrettifiedGson().toJson(data.getJsonElement(), writer);
                writer.close();

            } catch (Throwable throwable) {
                StorageInitializer.getInstance().getErrorHandler().accept(new IllegalStateException("Failed to save object with id: " + object.getKey(), throwable));
            }
        });
    }

}
