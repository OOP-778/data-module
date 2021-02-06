package com.oop.datamodule.postgresql;

import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.commonsql.database.HikariCPDatabase;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.List;

public class PostgreDatabase extends HikariCPDatabase {
  public PostgreDatabase(PostgreSQLCredential credential) {
    HikariConfig config = new HikariConfig();
    config.setPoolName("DM-Postgre-Pool");
    config.setJdbcUrl(credential.toURL());
    config.setDriverClassName("org.postgresql.Driver");
    config.setUsername(credential.username());
    config.setPassword(credential.password());

    setDbSource(new HikariDataSource(config));
  }

  @Override
  public String getType() {
    return "PostgreSQL";
  }

  @Override
  public String columnEscaper() {
    return "\"";
  }

  @Override
  public void dropColumn(String table, String... dropsColumns) {
    List<String> columns = getColumns(table);
    for (String dropsColumn : dropsColumns) {
      if (!columns.contains(dropsColumn)) continue;
      execute("ALTER TABLE " + table + " DROP COLUMN " + dropsColumn);
    }
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
              + " \""
              + renamedColumn.getValue()
              + "\" TEXT");
    }
  }
}
