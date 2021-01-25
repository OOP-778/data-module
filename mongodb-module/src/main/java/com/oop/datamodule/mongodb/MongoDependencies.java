package com.oop.datamodule.mongodb;

import com.oop.datamodule.api.loader.Library;
import com.oop.datamodule.api.loader.StorageDependencies;

public class MongoDependencies extends StorageDependencies {
    public MongoDependencies() {
        try {
            Class.forName("com.mongodb.client.MongoClient");
        } catch (Throwable throwable) {
            addLib(Library.builder().from("org.mongodb:mongo-java-driver:3.12.2").build());
        }
    }
}
