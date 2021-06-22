package com.oop.datamodule.commonsql.database;

import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.commonsql.util.TableCreator;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.oop.datamodule.commonsql.util.SqlUtil.escapeColumn;
import static com.oop.datamodule.commonsql.util.SqlUtil.formatSQL;

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
                statement.setFetchSize(1);

                try (ResultSet rs = statement.executeQuery(formatSQL("SELECT * FROM {}", table))) {
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
                while (resultSet.next()) {
                  tables.add(resultSet.getString(3));
                }
              } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to get tables", throwable);
              }
            })
        .evict();
    return tables;
  }

  public synchronized boolean isPrimaryKeyUsed(String table, String keyColumn, String value) {
    if (!value.endsWith("\"") && !value.startsWith("\"")) value = "\"" + value + "\"";

    String finalValue = value;
    String escapedColumn = escapeColumn(keyColumn, this);

    return getConnection()
        .provideAndEvict(
            conn -> {
              try (PreparedStatement preparedStatement =
                  conn.prepareStatement(
                      formatSQL(
                          "SElECT {} FROM {} WHERE {} = '{}'",
                          escapedColumn,
                          table,
                          escapedColumn,
                          finalValue))) {
                preparedStatement.setFetchSize(1);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                  return resultSet.next() && resultSet.getObject(1) != null;
                }
              } catch (Throwable e) {
                throw new IllegalStateException(
                    "Failed to check if table contains value (table="
                        + table
                        + ", "
                        + escapedColumn
                        + "="
                        + finalValue
                        + ") cause of "
                        + e.getMessage(),
                    e);
              }
            });
  }

  @SneakyThrows
  public synchronized void remove(String table, String column, String value) {
    if (!value.startsWith("\"")) {
      value = "\"" + value + "\"";
    }
    column = escapeColumn(column, this);

    String finalValue = value;
    String finalColumn = column;
    execute(formatSQL("DELETE FROM {} WHERE {} = '{}'", table, finalColumn, finalValue));
  }

  public abstract void renameColumn(String table, DataPair<String, String>... pairs);

  public abstract void dropColumn(String table, String... columns);

  public abstract void shutdown();

  public SerializedData getValuesFromTable(
      String table, Set<String> columns, String pkey, String pValue) {
    final SerializedData data = new SerializedData();
    String columnsSelector =
        columns.stream()
            .map(column -> escapeColumn(column, this))
            .collect(Collectors.joining(", "));

    getConnection()
        .use(
            connection -> {
              try (ResultSet rs =
                  connection
                      .createStatement()
                      .executeQuery(
                          formatSQL(
                              "SELECT {} FROM {} WHERE {} = '{}'",
                              columnsSelector,
                              table,
                              escapeColumn(pkey, this),
                              pValue))) {
                rs.setFetchSize(1);

                while (rs.next()) {
                  for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                    data.getJsonElement()
                        .getAsJsonObject()
                        .addProperty(rs.getMetaData().getColumnName(i), rs.getString(i));
                  }
                }
              }
            })
        .evict();

    return data;
  }
}
