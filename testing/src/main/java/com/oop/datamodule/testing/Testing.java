package com.oop.datamodule.testing;

import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.mongodb.MongoCredential;
import com.oop.datamodule.mysql.MySQLCredential;
import com.oop.datamodule.postgresql.PostgreSQLCredential;
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
                .POSTGRE
                .provide(
                        objects.getLinker(),
                        new PostgreSQLCredential()
                            .database("postgres")
                            .hostname("localhost")
                            .username("postgres")
                            .password("Pavilas45")
                        )
        );
        objects.load();

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
