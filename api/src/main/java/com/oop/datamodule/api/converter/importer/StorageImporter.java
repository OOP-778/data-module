package com.oop.datamodule.api.converter.importer;

import com.google.gson.JsonObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.converter.BytesBuffer;
import com.oop.datamodule.api.model.ModelBody;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.api.util.Preconditions;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class StorageImporter {
    private List<Storage<? extends ModelBody>> storages = new ArrayList<>();

    public StorageImporter(List<Storage<ModelBody>> storages) {
        this.storages.addAll(storages);
    }

    public StorageImporter(StorageRegistry registry) {
        this.storages.addAll(registry.getStorages());
    }

    @SneakyThrows
    public Map<String, List<ModelBody>> importData(File file) {
        Preconditions.checkArgument(file.exists(), "Failed to import data from " + file.getName() + " cause it doesn't exist!");

        FileInputStream stream = new FileInputStream(file);
        GZIPInputStream inputStream = new GZIPInputStream(stream);

        final Map<String, List<ModelBody>> deserializedData = new HashMap<>();
        try {
            while (inputStream.available() != 0) {
                BytesBuffer bytesBuffer = BytesBuffer.fromStream(inputStream);
                if (bytesBuffer == null) continue;

                String key = bytesBuffer.readString();

                List<String> variantData = new ArrayList<>();
                bytesBuffer.readList(variantData, bytesBuffer::readString);
                bytesBuffer.clear();

                Class<ModelBody> modelBodyClass = null;
                for (Storage<? extends ModelBody> storage : storages) {
                    modelBodyClass = (Class<ModelBody>) storage.getVariants().get(key);
                    if (modelBodyClass != null)
                        break;
                }

                Preconditions.checkArgument(modelBodyClass != null, "Failed to find model by variant " + key);

                for (String variantDatum : variantData) {
                    JsonObject jsonObject = StorageInitializer.getInstance().getGson().fromJson(variantDatum, JsonObject.class);
                    SerializedData data = new SerializedData(jsonObject);

                    ModelBody object = modelBodyClass.getDeclaredConstructor().newInstance();
                    object.deserialize(data);

                    deserializedData
                            .computeIfAbsent(key, $ -> new ArrayList<>())
                            .add(object);
                }
            }
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to import data from " + file.getName(), throwable);

        } finally {
            inputStream.close();
        }

        return deserializedData;
    }
}
