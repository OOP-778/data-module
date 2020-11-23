package com.oop.datamodule.mongodb;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Objects;

@Accessors(chain = true, fluent = true)
@Setter
public class MongoBuilder {
    private int port = 27017;
    private String host;

    private String database;
    private String username;
    private String password;
    private String connectionUri;
    private MongoClient client;

    public MongoClient build() {
        if (client != null) return client;

        if (connectionUri != null)
            return new MongoClient(new MongoClientURI(connectionUri));
        else {
            ServerAddress address = new ServerAddress(host, port);
            MongoCredential credential = MongoCredential.createCredential(
                    username,
                    database,
                    password.toCharArray()
            );

            return new MongoClient(address, credential, MongoClientOptions.builder().build());
        }
    }

    public MongoDatabase buildAndGetDb() {
        Objects.requireNonNull(database, "Database not provided");
        return build().getDatabase(database);
    }
}
