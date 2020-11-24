package com.oop.datamodule.sqlite;

import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.commonsql.database.SQLDatabase;
import com.oop.datamodule.commonsql.util.TableCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SQLiteDatabase extends SQLDatabase {
    private final String path;

    public SQLiteDatabase(String path) {
        this.path = path;
    }

    public SQLiteDatabase(File folder, String name) {
        this("jdbc:sqlite:" + folder.getAbsolutePath() + File.separator + name + ".db");
        if (!folder.exists())
            folder.mkdirs();
    }

    @Override
    protected Connection provideConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(path);
    }

    @SneakyThrows
    public void dropColumn(String table, String ...columnsToDropArray) {
        ResultSet resultSet = getConnection().createStatement().executeQuery("select * from " + table);

        ResultSet primaryKeys = getConnection().getMetaData().getPrimaryKeys(null, null, table);
        primaryKeys.next();
        String primaryKeysString = primaryKeys.getString("COLUMN_NAME");

        DataPair<String, String> primaryKey = null;
        List<DataPair<String, String>> columns = new ArrayList<>();

        List<String> columnsToDrop = Arrays.asList(columnsToDropArray);

        for (int index = 1; index <= resultSet.getMetaData().getColumnCount(); index++) {
            String columnName = resultSet.getMetaData().getColumnName(index);

            // Check if columnName is primary key
            if (columnName.contentEquals(primaryKeysString)) {
                primaryKey = new DataPair<>(columnName, resultSet.getMetaData().getColumnTypeName(index));
                continue;
            }

            columns.add(new DataPair<>(columnName, resultSet.getMetaData().getColumnTypeName(index)));
        }

        // Create new table with the new column names
        TableCreator creator = newTableCreator();
        creator.primaryKey(primaryKey.getKey(), primaryKey.getValue());

        for (DataPair<String, String> column : columns) {
            if (columnsToDrop.contains(column.getKey())) continue;
            creator.addColumn(column.getKey(), column.getValue());
        }

        creator.setName(table + "_clone");
        creator.create();

        // Copy all the data over to clone table
        List<String> structure = new LinkedList<>();
        structure.add(primaryKey.getKey());
        structure.addAll(columns.stream().map(DataPair::getKey).collect(Collectors.toList()));

        String[] oldStructure = structure.toArray(new String[0]);
        structure.removeIf(columnsToDrop::contains);

        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ").append(table + "_clone").append(" (").append(String.join(",", structure)).append(") VALUES (");
        builder.append(structure.stream().map(s -> "?").collect(Collectors.joining(",")));
        builder.append(")");

        PreparedStatement insertStatement = getConnection().prepareStatement(builder.toString());

        List<List<DataPair<String, String>>> allValuesOf = getAllValuesOf(table, oldStructure);
        for (List<DataPair<String, String>> dataPairs : allValuesOf) {
            int index = 1;
            for (DataPair<String, String> dataPair : dataPairs) {
                if (columnsToDrop.contains(dataPair.getKey())) continue;
                insertStatement.setString(index, dataPair.getValue());
                index++;
            }
            insertStatement.executeUpdate();
        }
        getConnection().close();

        execute("DROP TABLE " + table);
        execute("ALTER TABLE " + table + "_clone RENAME TO " + table);
    }

    @SneakyThrows
    public void renameColumn(String table, DataPair<String, String> ...columnsModified) {
        ResultSet resultSet = getConnection().createStatement().executeQuery("select * from " + table);

        ResultSet primaryKeys = getConnection().getMetaData().getPrimaryKeys(null, null, table);
        primaryKeys.next();
        String primaryKeysString = primaryKeys.getString("COLUMN_NAME");

        DataPair<String, String> primaryKey = null;
        List<DataPair<String, String>> columns = new ArrayList<>();

        for (int index = 1; index <= resultSet.getMetaData().getColumnCount(); index++) {
            String columnName = resultSet.getMetaData().getColumnName(index);

            // Check if columnName is primary key
            if (columnName.contentEquals(primaryKeysString)) {
                primaryKey = new DataPair<>(columnName, resultSet.getMetaData().getColumnTypeName(index));
                continue;
            }

            columns.add(new DataPair<>(columnName, resultSet.getMetaData().getColumnTypeName(index)));
        }

        Map<String, String> oldColumnToNew = new HashMap<>();
        for (DataPair<String, String> stringStringDataPair : columnsModified)
            oldColumnToNew.put(stringStringDataPair.getKey(), stringStringDataPair.getValue());

        // Create new table with the new column names
        TableCreator creator = newTableCreator();
        creator.primaryKey(primaryKey.getKey(), primaryKey.getValue());

        for (DataPair<String, String> column : columns) {
            String newName = oldColumnToNew.get(column.getKey());
            if (newName != null)
                creator.addColumn(newName, column.getValue());
            else
                creator.addColumn(column.getKey(), column.getValue());
        }

        creator.setName(table + "_clone");
        creator.create();

        // Copy all the data over to clone table
        List<String> structure = new LinkedList<>();
        structure.add(primaryKey.getKey());
        structure.addAll(columns.stream().map(DataPair::getKey).collect(Collectors.toList()));

        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ").append(table + "_clone").append(" (").append(structure.stream().map(column -> {
            String newName = oldColumnToNew.get(column);
            if (newName != null)
                return newName;
            return column;
        }).collect(Collectors.joining(","))).append(") VALUES (");
        builder.append(structure.stream().map(s -> "?").collect(Collectors.joining(",")));
        builder.append(")");

        PreparedStatement insertStatement = getConnection().prepareStatement(builder.toString());

        List<List<DataPair<String, String>>> allValuesOf = getAllValuesOf(table, structure.toArray(new String[0]));
        for (List<DataPair<String, String>> dataPairs : allValuesOf) {
            int index = 1;
            for (DataPair<String, String> dataPair : dataPairs) {
                insertStatement.setString(index, dataPair.getValue());
                index++;
            }
            insertStatement.executeUpdate();
        }
        getConnection().close();

        execute("DROP TABLE " + table);
        execute("ALTER TABLE " + table + "_clone RENAME TO " + table);
    }

    @Getter
    @AllArgsConstructor
    public static class TableStruct {
        private DataPair<String, String> primarykey;
        private List<DataPair<String, String>> columns;
    }

    @Override
    public String getType() {
        return "SQLITE";
    }
}