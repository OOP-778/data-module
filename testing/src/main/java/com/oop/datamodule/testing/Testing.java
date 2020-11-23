package com.oop.datamodule.testing;

import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.mongodb.MongoBuilder;
import com.oop.datamodule.mysql.MySQLCredential;
import com.oop.datamodule.universal.StorageProviders;

import java.util.UUID;

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
                        .MONGO_DB
                        .provide(
                                objects.getLinker(),
                                new MongoBuilder()
                                        .connectionUri("...")
                                        .database("admin")
                                        .buildAndGetDb()
                        )
        );
        objects.load();

        objects.currentImplementation(
                StorageProviders
                        .MYSQL
                        .provide(
                                objects.getLinker(),
                                new MySQLCredential()
                        )
        );
        objects.load();


        objects.add(new Object(UUID.randomUUID()));
        objects.stream()
                .forEach(ob -> {
                    ob.getCounts().add(2);
                    ob.getSubObjects().add(new SubObject());
                    ob.setCoins(ob.getCoins() + 1);
                });
//
//        objects.remove(objects.stream().findFirst().get());
        objects.save();
    }
}
