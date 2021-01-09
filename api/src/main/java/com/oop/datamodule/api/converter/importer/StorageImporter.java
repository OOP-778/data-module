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
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class StorageImporter {
    private List<Storage> storages = new ArrayList<>();

    public StorageImporter(List<Storage> storages) {
        this.storages.addAll(storages);
    }

    public StorageImporter(Storage... storages) {
        this(Arrays.asList(storages));
    }

    public StorageImporter(StorageRegistry registry) {
        this.storages.addAll(registry.getStorages());
    }

    @SneakyThrows
    public Map<String, List<ModelBody>> importData(File file) {
        if (!file.getName().endsWith("datapack"))
            file = new File(file.getParentFile(), file.getName() + ".datapack");

        Preconditions.checkArgument(file.exists(), "Failed to import data from " + file.getName() + " cause it doesn't exist!");

        FileInputStream stream = new FileInputStream(file);
        InflaterInputStream inputStream = new InflaterInputStream(stream);

        final Map<String, List<ModelBody>> deserializedData = new HashMap<>();
        try {
            byte[] size = new byte[4];
            while(inputStream.read(size) != -1) {
                BytesBuffer bytesBuffer = BytesBuffer.fromStream(inputStream, size);
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

                    Constructor<ModelBody> constructor = modelBodyClass.getDeclaredConstructor();
                    constructor.setAccessible(true);

                    ModelBody object = constructor.newInstance();
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
