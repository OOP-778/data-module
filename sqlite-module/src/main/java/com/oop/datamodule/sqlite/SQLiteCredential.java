package com.oop.datamodule.sqlite;

import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;

@Accessors(chain = true, fluent = true)
@Setter
public class SQLiteCredential {
    private File folder;
    private String database;
    private SQLiteDatabase sqlLiteDatabase;

    public SQLiteDatabase build() {
        if (sqlLiteDatabase != null)
            return sqlLiteDatabase;

        return new SQLiteDatabase(folder, database);
    }
}
