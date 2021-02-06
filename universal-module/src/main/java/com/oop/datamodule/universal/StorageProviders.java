package com.oop.datamodule.universal;

import com.oop.datamodule.universal.provider.JsonProvider;
import com.oop.datamodule.universal.provider.MongoDBProvider;
import com.oop.datamodule.universal.provider.StorageProvider;
import com.oop.datamodule.universal.provider.sql.H2Provider;
import com.oop.datamodule.universal.provider.sql.MySQLProvider;
import com.oop.datamodule.universal.provider.sql.PostgreSQLProvider;
import com.oop.datamodule.universal.provider.sql.SQLiteProvider;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StorageProviders {
  public static final MongoDBProvider MONGO_DB = new MongoDBProvider();
  public static final SQLiteProvider SQLITE = new SQLiteProvider();
  public static final MySQLProvider MYSQL = new MySQLProvider();
  public static final JsonProvider JSON = new JsonProvider();
  public static final PostgreSQLProvider POSTGRE = new PostgreSQLProvider();
  public static final H2Provider H2 = new H2Provider();

  private static final Map<String, StorageProvider> byName = new HashMap<>();

  static {
    for (Field field : StorageProviders.class.getFields()) {
      field.setAccessible(true);

      try {
        byName.put(
            field.getName().toLowerCase().replace("_", ""), (StorageProvider) field.get(null));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  public static <T extends StorageProvider> T getByName(String name) {
    return (T)
        Objects.requireNonNull(
            byName.get(name), "Storage Provider by name: " + name + " not found!");
  }
}
