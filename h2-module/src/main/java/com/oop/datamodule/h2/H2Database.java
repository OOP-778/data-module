package com.oop.datamodule.h2;

import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.commonsql.database.SQLDatabase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.h2.jdbc.JdbcConnection;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import static com.oop.datamodule.commonsql.util.SqlUtil.formatSQL;

public class H2Database extends SQLDatabase {
  private final String path;
  private Connection connection;

  public H2Database(String path) {
    this.path = path;
  }

  public H2Database(File folder, String name) {
    this(
        "jdbc:h2:file:"
            + folder.getAbsolutePath()
            + File.separator
            + name
            + ";DATABASE_TO_UPPER=false");
    if (!folder.exists()) folder.mkdirs();
  }

  @Override
  @SneakyThrows
  protected Connection provideConnection() {
    if (connection == null || connection.isClosed()) {
      try {
        Class.forName("org.h2.jdbc.JdbcConnection");
        connection = new JdbcConnection(path, new Properties());
      } catch (Throwable throwable) {
        throw new IllegalStateException("Failed to get connection at " + path, throwable);
      }
    }

    return connection;
  }

  @SneakyThrows
  public synchronized void dropColumn(String table, String... columnsToDropArray) {
    getConnection()
        .use(
            conn -> {
              try {
                execute(
                    formatSQL(
                        "ALTER TABLE {} IF EXISTS DROP COLUMN IF EXISTS ({})",
                        table,
                        String.join(", ", columnsToDropArray)));
              } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to drop column", throwable);
              }
            });
  }

  @SneakyThrows
  public synchronized void renameColumn(String table, DataPair<String, String>... columnsModified) {
    getConnection()
        .use(
            conn -> {
              try {
                for (DataPair<String, String> columnData : columnsModified) {
                  execute(
                      formatSQL(
                          "ALTER TABLE {} IF EXISTS ALTER COLUMN IF EXISTS {} RENAME TO {}",
                          table,
                          columnData.getKey(),
                          columnData.getValue()));
                }
              } catch (Throwable throwable) {
                throw new IllegalStateException("failed to rename columns", throwable);
              }
            });
  }

  @Override
  public String getType() {
    return "H2";
  }

  @Override
  public String columnEscaper() {
    return "";
  }

  @Override
  public void evict(Connection connection) {
    // Do nothing
  }

  @Override
  @SneakyThrows
  public void shutdown() {
    connection.close();
  }
}
