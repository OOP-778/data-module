package com.oop.datamodule.api.converter.exporter;

import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.converter.BytesBuffer;
import com.oop.datamodule.api.model.ModelBody;
import com.oop.datamodule.api.storage.Storage;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

public class StorageExporter {
    private final List<Storage> storageList = new ArrayList<>();

    public StorageExporter(List<Storage> storages) {
        this.storageList.addAll(storages);
    }

    public StorageExporter(Storage ...storages) {
        this(Arrays.asList(storages));
    }

    public StorageExporter(StorageRegistry registry) {
        this.storageList.addAll(registry.getStorages());
    }

    @SneakyThrows
    public void export(File directory, String name) {
        File exportFile = new File(directory, name + ".datapack");

        if (!directory.exists())
            directory.mkdirs();

        if (!exportFile.exists())
            exportFile.delete();

        // Variant Name ~ 4 bytes
        // Size ~ 4 bytes
        Map<String, List<String>> objectsByVariants = new HashMap<>();

        for (Storage storage : storageList) {
            for (Object modelBody : storage) {
                SerializedData data = new SerializedData();
                ((ModelBody) modelBody).serialize(data);

                objectsByVariants
                        .computeIfAbsent(storage.findVariantNameFor(modelBody.getClass()), key -> new ArrayList<>())
                        .add(StorageInitializer.getInstance().getGson().toJson(data.getJsonElement()));
            }
        }

        DeflaterOutputStream outputStream = new DeflaterOutputStream(new FileOutputStream(exportFile));

        BytesBuffer buffer = new BytesBuffer();
        objectsByVariants.forEach((key, objects) -> {
            try {
                // Write variant
                buffer.writeString(key);

                // Write objects
                buffer.writeList(objects, buffer::writeString);

                // Write object
                buffer.append(outputStream);

                // Cleanup
                buffer.clear();

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        outputStream.flush();
        outputStream.close();
    }
}
