package com.oop.datamodule.testing;

import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.converter.exporter.StorageExporter;
import com.oop.datamodule.api.converter.importer.StorageImporter;
import com.oop.datamodule.api.model.ModelBody;
import com.oop.datamodule.mongodb.MongoCredential;
import com.oop.datamodule.mysql.MySQLCredential;
import com.oop.datamodule.postgresql.PostgreSQLCredential;
import com.oop.datamodule.universal.StorageProviders;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Testing {
    public static void main(String[] args) {
        StorageRegistry registry = new StorageRegistry();

        StorageInitializer.initialize(
                Runnable::run,
                Runnable::run,
                null
        );

        ObjectStorage objects = new ObjectStorage(registry);
        objects.currentImplementation(
                StorageProviders
                        .POSTGRE
                        .provide(
                                objects.getLinker(),
                                new PostgreSQLCredential()
                                        .database("postgres")
                                        .hostname("localhost")
                                        .username("postgres")
                        )
        );
        //objects.load();

//        StorageExporter exporter = new StorageExporter(objects);
//        exporter.export(new File("test"), "test");

        StorageImporter importer = new StorageImporter(objects);
        Map<String, List<ModelBody>> stringListMap = importer.importData(new File("test", "test"));

        System.out.println("loaded data " + stringListMap.keySet().stream()
                .map(key -> key + "=" + stringListMap.get(key))
                .collect(Collectors.joining(", ", "{", "}")));
    }
}
