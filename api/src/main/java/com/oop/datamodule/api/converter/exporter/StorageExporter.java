package com.oop.datamodule.api.converter.exporter;

import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.converter.BytesWriter;
import com.oop.datamodule.api.model.ModelBody;
import com.oop.datamodule.api.storage.Storage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DeflaterOutputStream;
import lombok.SneakyThrows;

public class StorageExporter {
  private final List<Storage> storageList = new ArrayList<>();

  public StorageExporter(List<Storage> storages) {
    this.storageList.addAll(storages);
  }

  public StorageExporter(Storage... storages) {
    this(Arrays.asList(storages));
  }

  public StorageExporter(StorageRegistry registry) {
    this.storageList.addAll(registry.getStorages());
  }

  @SneakyThrows
  public long export(File directory, String name) {
    File exportFile = new File(directory, name + ".datapack");

    if (!directory.exists()) directory.mkdirs();

    if (!exportFile.exists()) exportFile.delete();

    // Variant Name ~ 4 bytes
    // Size ~ 4 bytes
    Map<String, List<String>> objectsByVariants = new HashMap<>();

    for (Storage storage : storageList) {
      for (Object modelBody : storage) {
        SerializedData data = new SerializedData();
        ((ModelBody) modelBody).serialize(data);

        objectsByVariants
            .computeIfAbsent(
                storage.findVariantNameFor(modelBody.getClass()), key -> new ArrayList<>())
            .add(StorageInitializer.getInstance().getGson().toJson(data.getJsonElement()));
      }
    }

    AtomicLong size = new AtomicLong();
    FileOutputStream outputStream = new FileOutputStream(exportFile);
    DeflaterOutputStream output = new DeflaterOutputStream(outputStream);

    AtomicLong exported = new AtomicLong(0);

    BytesWriter writer = new BytesWriter();
    objectsByVariants.forEach(
        (key, objects) -> {
          try {
            // Write variant
            writer.writeString(key);

            // Write objects
            writer.writeList(objects, writer::writeString);

            // Write object
            byte[] done = writer.done();

            size.addAndGet(done.length);
            output.write(done);

            exported.addAndGet(objects.size());
          } catch (Throwable throwable) {
            throwable.printStackTrace();
          }
        });

    output.flush();
    output.close();

    return exported.get();
  }
}
