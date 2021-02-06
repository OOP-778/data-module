package com.oop.datamodule.commonsql.database;

import static com.oop.datamodule.commonsql.util.SqlUtil.escapeColumn;

import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.commonsql.util.TableCreator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public abstract class SQLDatabase {
  public abstract String getType();

  public abstract String columnEscaper();

  public abstract void evict(Connection connection);

  @SneakyThrows
  public synchronized ConnectionWrapper getConnection() {
    return new ConnectionWrapper(provideConnection(), this::evict);
  }

  protected abstract Connection provideConnection();

  public List<String> getColumns(String table) {
    List<String> columns = new ArrayList<>();

    getConnection()
        .use(
            connection -> {
              try (Statement statement = connection.createStatement()) {
                try (ResultSet rs = statement.executeQuery("SELECT * FROM " + table)) {
                  ResultSetMetaData data = rs.getMetaData();
                  int index = 1;
                  int columnLen = data.getColumnCount();
                  while (index <= columnLen) {
                    columns.add(data.getColumnName(index));
                    index++;
                  }
                }
              } catch (Throwable e) {
                throw new IllegalStateException("Failed to get columns of table " + table, e);
              }
            })
        .evict();

    return columns;
  }

  public synchronized void execute(String sql) {
    getConnection()
        .use(
            conn -> {
              try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.execute();
              } catch (SQLException e) {
                throw new IllegalStateException("Failed to execute sql '" + sql + "'", e);
              }
            })
        .evict();
  }

  public TableCreator newTableCreator() {
    return new TableCreator(this);
  }

  public synchronized List<String> getTables() {
    List<String> tables = new ArrayList<>();
    getConnection()
        .use(
            conn -> {
              try (ResultSet resultSet = conn.getMetaData().getTables(null, null, null, null)) {
                while (resultSet.next()) tables.add(resultSet.getString(3));
              } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to get tables", throwable);
              }
            })
        .evict();
    return tables;
  }

  public synchronized boolean isPrimaryKeyUsed(
      String table, String[] structure, String primaryKey) {
    if (!primaryKey.endsWith("\"") && !primaryKey.startsWith("\""))
      primaryKey = "\"" + primaryKey + "\"";

    String finalPrimaryKey = primaryKey;
    return getConnection()
        .provideAndEvict(
            conn -> {
              try (PreparedStatement preparedStatement =
                  conn.prepareStatement(
                      "SELECT "
                          + escapeColumn(structure[0], this)
                          + " from "
                          + table
                          + " where "
                          + escapeColumn(structure[0], this)
                          + " = '"
                          + finalPrimaryKey
                          + "'")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                  return resultSet.next() && resultSet.getObject(1) != null;
                }
              } catch (Throwable e) {
                throw new IllegalStateException(
                    "Failed to check if table contains value (table="
                        + table
                        + ", "
                        + structure[0]
                        + "="
                        + finalPrimaryKey
                        + ") cause of "
                        + e.getMessage(),
                    e);
              }
            });
  }

  public synchronized List<List<DataPair<String, String>>> getAllValuesOf(
      String table, String[] structure) {
    List<List<DataPair<String, String>>> allData = new LinkedList<>();
    getConnection()
        .use(
            conn -> {
              try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM " + table)) {
                while (rs.next()) {
                  int i = 1;
                  List<DataPair<String, String>> data = new LinkedList<>();
                  for (String column : structure) {
                    data.add(new DataPair<>(rs.getMetaData().getColumnName(i), rs.getString(i)));
                    i++;
                  }
                  allData.add(data);
                }
              }
            })
        .evict();
    return allData;
  }

  @SneakyThrows
  public synchronized void remove(String table, String[] structure, String primaryKey) {
    if (!primaryKey.startsWith("\"")) primaryKey = "\"" + primaryKey + "\"";

      String finalPrimaryKey = primaryKey;
      getConnection()
        .use(conn -> {
            try {
                conn.createStatement()
                    .execute(
                        "DELETE FROM "
                            + table
                            + " WHERE "
                            + escapeColumn(structure[0], this)
                            + " = '"
                            + finalPrimaryKey
                            + "'");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        })
        .evict();
  }

  public abstract void renameColumn(String table, DataPair<String, String>... pairs);

  public abstract void dropColumn(String table, String... columns);

  public abstract void shutdown();
}
