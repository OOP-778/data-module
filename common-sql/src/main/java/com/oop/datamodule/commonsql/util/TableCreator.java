package com.oop.datamodule.commonsql.util;

import com.oop.datamodule.commonsql.database.SQLDatabase;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.oop.datamodule.commonsql.util.SqlUtil.escapeColumn;

public class TableCreator {
  private final SQLDatabase database;
  private final Map<String, String> columnMap = new LinkedHashMap<>();
  private String name;
  private String primaryKey;

  public TableCreator(SQLDatabase database) {
    this.database = database;
  }

  public TableCreator setName(String name) {
    this.name = name;
    return this;
  }

  public TableCreator addColumn(String columnName, Column columnType) {
    return addColumn(columnName, columnType.getSql());
  }

  public TableCreator addColumn(String columnName, String columnType) {
    columnMap.put(columnName, columnType);
    return this;
  }

  public TableCreator primaryKey(String column) {
    this.primaryKey = column;
    return this;
  }

  public SQLDatabase create() {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (");

    if (primaryKey != null) {
      String sqlType = columnMap.get(primaryKey);
      if (database.getType().equalsIgnoreCase("SQLITE")) {
        queryBuilder.append(primaryKey).append(" ").append(sqlType).append(" PRIMARY KEY, ");

      } else
        queryBuilder
            .append(escapeColumn(primaryKey, database))
            .append(" ")
            .append(sqlType)
            .append(", ");
    }

    boolean first = true;
    for (Map.Entry<String, String> columnPair : columnMap.entrySet()) {
      if (first) {
        queryBuilder
            .append(escapeColumn(columnPair.getKey(), database))
            .append(" ")
            .append(columnPair.getValue());
        first = false;

      } else
        queryBuilder
            .append(", ")
            .append(escapeColumn(columnPair.getKey(), database))
            .append(" ")
            .append(columnPair.getValue());
    }

    if (primaryKey != null && !(database.getType().equalsIgnoreCase("SQLITE")))
      queryBuilder.append(", PRIMARY KEY (").append(primaryKey).append(")");

    queryBuilder.append(")");
    database.execute(queryBuilder.toString());

    return database;
  }
}
