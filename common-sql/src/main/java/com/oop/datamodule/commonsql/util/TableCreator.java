package com.oop.datamodule.commonsql.util;

import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.commonsql.database.SQLDatabase;

import java.util.LinkedList;
import java.util.List;

import static com.oop.datamodule.commonsql.util.SqlUtil.escapeColumn;

public class TableCreator {
    private final SQLDatabase database;
    private String name;
    private final List<DataPair<String, String>> columns = new LinkedList<>();
    private DataPair<String, String> primaryKey;

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
        columns.add(new DataPair<>(columnName, columnType));
        return this;
    }

    public TableCreator primaryKey(String column, Column columnType) {
        return primaryKey(column, columnType.getSql());
    }

    public TableCreator primaryKey(String column, String columnType) {
        primaryKey = new DataPair<>(column, columnType);
        return this;
    }

    public SQLDatabase create() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (");

        if (primaryKey != null) {
            if (database.getType().equalsIgnoreCase("SQLITE")) {
                queryBuilder
                        .append(primaryKey.getKey())
                        .append(" ")
                        .append(primaryKey.getValue())
                        .append(" PRIMARY KEY, ");

            } else
                queryBuilder
                        .append(escapeColumn(primaryKey.getKey(), database))
                        .append(" VARCHAR(255)")
                        .append(", ");
        }

        boolean first = true;
        for (DataPair<String, String> columnPair : columns) {
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
            queryBuilder
                    .append(", PRIMARY KEY (")
                    .append(primaryKey.getKey())
                    .append(")");

        queryBuilder.append(")");
        database.execute(queryBuilder.toString());

        return database;
    }
}