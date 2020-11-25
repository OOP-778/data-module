package com.oop.datamodule.commonsql.database;

public interface SqlCredential {
    SQLDatabase build();

    boolean test();
}
