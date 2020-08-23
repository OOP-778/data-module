package com.oop.datamodule.database;

import com.oop.datamodule.database.types.SqlLiteDatabase;
import com.oop.datamodule.util.DataPair;

import java.util.LinkedList;
import java.util.List;

public class TableCreator {

    private final DatabaseWrapper database;
    private String name;
    private final List<DataPair<String, String>> columns = new LinkedList<>();

    private DataPair<String, String> primaryKey;

    TableCreator(DatabaseWrapper database) {
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

    public DatabaseWrapper create() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (");

        if (primaryKey != null) {
            if (database instanceof SqlLiteDatabase) {
                queryBuilder.append(primaryKey.getKey()).append(" ").append(primaryKey.getValue()).append(" PRIMARY KEY, ");

            } else
                queryBuilder.append(primaryKey.getKey()).append(" VARCHAR(255)").append(", ");
        }

        boolean first = true;
        for (DataPair<String, String> columnPair : columns) {
            if (first) {
                queryBuilder.append(columnPair.getKey()).append(" ").append(columnPair.getValue());
                first = false;

            } else
                queryBuilder.append(", ").append(columnPair.getKey()).append(" ").append(columnPair.getValue());
        }

        if (primaryKey != null && !(database instanceof SqlLiteDatabase))
            queryBuilder.append(", PRIMARY KEY (").append(primaryKey.getKey()).append(")");

        queryBuilder.append(")");
        database.execute(queryBuilder.toString());

        return database;
    }
}