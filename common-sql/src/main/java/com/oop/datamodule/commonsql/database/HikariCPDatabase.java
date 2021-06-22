package com.oop.datamodule.commonsql.database;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.Connection;

public abstract class HikariCPDatabase extends SQLDatabase {
  @Setter(AccessLevel.PROTECTED)
  @NonNull
  private HikariDataSource dbSource;

  @SneakyThrows
  public HikariCPDatabase() {}

  @Override
  @SneakyThrows
  protected Connection provideConnection() {
    return dbSource.getConnection();
  }

  @Override
  public void shutdown() {
    dbSource.close();
  }

  @Override
  @SneakyThrows
  public void evict(Connection connection) {
    connection.close();
  }
}
