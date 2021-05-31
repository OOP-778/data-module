package com.oop.datamodule.mysql;

import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.commonsql.database.HikariCPDatabase;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabase extends HikariCPDatabase {
  public MySQLDatabase(MySQLCredential props) {
    HikariConfig config = new HikariConfig();
    config.setPoolName("DM-MySQL-Pool");
    config.setDriverClassName("com.mysql.jdbc.Driver");

    config.setUsername(props.username());
    config.setPassword(props.password());

    config.setJdbcUrl(props.toURL());

    config.addDataSourceProperty("cachePrepStmts", true);
    config.addDataSourceProperty("prepStmtCacheSize", 250);
    config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
    config.addDataSourceProperty("useServerPrepStmts", true);
    config.addDataSourceProperty("useLocalSessionState", true);
    config.addDataSourceProperty("rewriteBatchedStatements", true);
    config.addDataSourceProperty("cacheResultSetMetadata", true);
    config.addDataSourceProperty("cacheServerConfiguration", true);
    config.addDataSourceProperty("elideSetAutoCommits", true);
    config.addDataSourceProperty("maintainTimeStats", false);
    config.addDataSourceProperty("alwaysSendSetIsolation", false);
    config.addDataSourceProperty("cacheCallableStmts", true);

    setDbSource(new HikariDataSource(config));
  }

  @Override
  public List<String> getTables() {
    List<String> tables = new ArrayList<>();
    getConnection()
        .use(
            conn -> {
              try (PreparedStatement statement =
                  conn.prepareStatement(
                      "SELECT table_name FROM information_schema.tables WHERE table_type = 'base table'")) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                  tables.add(resultSet.getString(1));
                }
              } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to get tables", throwable);
              }
            })
        .evict();
    return tables;
  }

  @Override
  public void renameColumn(String table, DataPair<String, String>... pairs) {
    List<String> columns = getColumns(table);
    for (DataPair<String, String> renamedColumn : pairs) {
      if (!columns.contains(renamedColumn.getKey())) continue;
      execute(
          "ALTER TABLE "
              + table
              + " CHANGE "
              + renamedColumn.getKey()
              + " "
              + renamedColumn.getValue()
              + " TEXT");
    }
  }

  @Override
  public void dropColumn(String table, String... dropsColumns) {
    List<String> columns = getColumns(table);
    for (String dropsColumn : dropsColumns) {
      if (!columns.contains(dropsColumn)) continue;
      execute("ALTER TABLE " + table + "DROP COLUMN " + dropsColumn);
    }
  }

  @Override
  public String getType() {
    return "MYSQL";
  }

  @Override
  public String columnEscaper() {
    return "";
  }
}
