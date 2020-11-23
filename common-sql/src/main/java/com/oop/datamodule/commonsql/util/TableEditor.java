package com.oop.datamodule.commonsql.util;

import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.commonsql.database.SQLDatabase;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TableEditor {
    private final String table;
    private final List<DataPair<String, String>> addColumns = new ArrayList<>();
    private final List<String> dropsColumns = new ArrayList<>();
    private final List<DataPair<String, String>> renamedColumns = new ArrayList<>();

    public TableEditor addColumn(String name, String type) {
        addColumns.add(new DataPair<>(name, type));
        return this;
    }

    public TableEditor addDropColumn(String column) {
        dropsColumns.add(column);
        return this;
    }

    public TableEditor renameColumn(String oldColumnName, String newColumnName) {
        renamedColumns.add(new DataPair<>(oldColumnName, newColumnName));
        return this;
    }

    public void edit(SQLDatabase database) {
        if (!database.getTables().contains(table)) return;

        // Add columns
        for (DataPair<String, String> column : addColumns)
            if (!database.getColumns(table).contains(column.getKey()))
                database.execute("ALTER TABLE " + table + " ADD " + column.getKey() + " " + column.getValue());

        // Rename columns
        if (!renamedColumns.isEmpty())
            database.renameColumn(table, renamedColumns.toArray(new DataPair[0]));

        // Drop columns
        if (!dropsColumns.isEmpty())
            database.dropColumn(table, dropsColumns.toArray(new String[0]));
    }
}