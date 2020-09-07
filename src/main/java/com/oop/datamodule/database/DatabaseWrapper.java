package com.oop.datamodule.database;

import com.oop.datamodule.util.DataPair;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.*;

@Getter
public abstract class DatabaseWrapper {
    private Connection connection;
    protected abstract Connection provideConnection() throws Throwable;

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = provideConnection();
            }
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to get connection. Perhaps incorrect params?", throwable);
        }

        return connection;
    }

    public synchronized List<String> getColumns(String table) {
        List<String> columns = new ArrayList<>();

        try (Statement statement = getConnection().createStatement()) {
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

        return columns;
    }

    public synchronized void execute(String sql) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to execute sql '" + sql + "'", e);
        }
    }

    public TableCreator newTableCreator() {
        return new TableCreator(this);
    }

    public synchronized List<String> getTables() {
        List<String> tables = new ArrayList<>();
        try (ResultSet resultSet = getConnection().getMetaData().getTables(null, null, null, null)) {
            while (resultSet.next())
                tables.add(resultSet.getString(3));
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to get tables", throwable);
        }
        return tables;
    }

    public synchronized boolean isPrimaryKeyUsed(String table, String[] structure, String primaryKey) {
        if (!primaryKey.endsWith("\"") && !primaryKey.startsWith("\""))
            primaryKey = "\"" + primaryKey + "\"";

        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT " + structure[0] + " from " + table + " where " + structure[0] + " = '" + primaryKey + "'")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getObject(1) != null;
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to check if table contains value (table=" + table + ", " + structure[0] + "=" + primaryKey + ") cause of " + e.getMessage(), e);
        }
    }

    @SneakyThrows
    public synchronized List<Set<DataPair<String, String>>> getAllValuesOf(String table, String[] structure) {
        List<Set<DataPair<String, String>>> allData = new ArrayList<>();
        try (ResultSet rs = getConnection().createStatement().executeQuery("SELECT * FROM " + table)) {
            while (rs.next()) {
                int i = 1;
                Set<DataPair<String, String>> data = new LinkedHashSet<>();
                for (String column : structure) {
                    data.add(new DataPair<>(column, rs.getString(i)));
                    i++;
                }
                allData.add(data);
            }
        }
        return allData;
    }

    @SneakyThrows
    public synchronized void remove(String table, String[] structure, String primaryKey) {
        if (!primaryKey.startsWith("\""))
            primaryKey = "\"" + primaryKey + "\"";

        getConnection().createStatement().execute("DELETE FROM " + table + " WHERE " + structure[0] + " = '" + primaryKey + "'");
    }
}
