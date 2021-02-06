package com.oop.datamodule.testing;

import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.converter.importer.StorageImporter;
import com.oop.datamodule.api.loader.logging.adapters.JDKLogAdapter;
import com.oop.datamodule.api.model.ModelBody;
import com.oop.datamodule.commonsql.database.HikariCPDatabase;
import com.oop.datamodule.h2.H2Credential;
import com.oop.datamodule.h2.H2Database;
import com.oop.datamodule.postgresql.PostgreSQLCredential;
import com.oop.datamodule.universal.StorageProviders;
import com.oop.datamodule.universal.provider.sql.SQLStorageImpl;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Testing {
  public static void main(String[] args) {

    StorageInitializer
        .initialize(
            Runnable::run,
            Runnable::run,
            new LibManager(new JDKLogAdapter(Logger.getLogger("LB")), new File("/").toPath()),
            null,
            Throwable::printStackTrace
        );

    StorageRegistry registry = new StorageRegistry();

    ObjectStorage objects = new ObjectStorage(registry);
    objects.currentImplementation(
        StorageProviders.H2.provide(
            objects.getLinker(),
            new H2Credential()
              .database("test")
              .folder(new File("."))
              .build()));

    H2Database database = (H2Database) ((SQLStorageImpl) objects.getCurrentImplementation())
        .getDatabase();
    System.out.println(Arrays.toString(database.getTables().toArray()));
    objects.load();

    objects.add(new Object(UUID.randomUUID()));

    for (Object object : objects) {
      object.setCoins(object.getCoins() + 1);
    }

    objects.save();
    // objects.load();

    //        StorageExporter exporter = new StorageExporter(objects);
    //        exporter.export(new File("test"), "test");
  }
}
