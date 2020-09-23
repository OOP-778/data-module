package com.oop.datamodule.database;

import com.oop.datamodule.database.types.SqlLiteDatabase;
import com.oop.datamodule.util.DataPair;
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

    public void edit(DatabaseWrapper database) {
        if (!database.getTables().contains(table)) return;

        // Add columns
        for (DataPair<String, String> column : addColumns)
            if (!database.getColumns(table).contains(column.getKey()))
                database.execute("ALTER TABLE " + table + " ADD " + column.getKey() + " " + column.getValue());

        // Rename columns
        if (!renamedColumns.isEmpty()) {
            if (database instanceof SqlLiteDatabase)
                ((SqlLiteDatabase) database).renameColumn(table, renamedColumns.toArray(new DataPair[0]));
            else
                for (DataPair<String, String> renamedColumn : renamedColumns)
                    database.execute("ALTER TABLE " + table + " CHANGE " + renamedColumn.getKey() + " " + renamedColumn.getValue() + " TEXT");
        }

        // Drop columns
        if (!dropsColumns.isEmpty()) {
            if (database instanceof SqlLiteDatabase)
                ((SqlLiteDatabase) database).dropColumn(table, dropsColumns.toArray(new String[0]));
            else
                for (String dropsColumn : dropsColumns)
                    database.execute("ALTER TABLE " + table + "DROP COLUMN " + dropsColumn);
        }
    }
}