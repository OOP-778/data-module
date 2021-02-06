package com.oop.datamodule.sqlite;

import com.oop.datamodule.commonsql.database.SqlCredential;
import java.io.File;
import java.sql.Connection;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@Setter
public class SQLiteCredential implements SqlCredential {
  private File folder;
  private String database;
  private SQLiteDatabase sqlLiteDatabase;

  public SQLiteDatabase build() {
    if (sqlLiteDatabase != null) return sqlLiteDatabase;

    return new SQLiteDatabase(folder, database);
  }

  @Override
  public boolean test() {
    try {
      SQLiteDatabase build = build();
      build.getConnection().use(conn -> {});
    } catch (Throwable throwable) {
      throw new IllegalStateException("SQLite database test failed...", throwable);
    }

    return true;
  }
}
