package com.oop.datamodule.mongodb;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@Setter
public class MongoCredential {
    private int port = 27017;
    private String hostname;

    private String database;
    private String username;
    private String password;
    private String connectionUri;
    private MongoClient client;
    private MongoDatabase mongoDatabase;

    public MongoDatabase build() {
        if (mongoDatabase != null) return mongoDatabase;
        if (client != null) return client.getDatabase(database);

        if (connectionUri != null)
            return new MongoClient(new MongoClientURI(connectionUri)).getDatabase(database);

        else {
            ServerAddress address = new ServerAddress(hostname, port);
            com.mongodb.MongoCredential credential = com.mongodb.MongoCredential.createCredential(
                    username,
                    database,
                    password.toCharArray()
            );

            return new MongoClient(address, credential, MongoClientOptions.builder().build()).getDatabase(database);
        }
    }

    public boolean test() {
        try {
            MongoDatabase build = build();
        } catch (Throwable throwable) {
            throw new IllegalStateException("MongoDB test failed...", throwable);
        }
        return true;
    }
}
