package com.oop.datamodule.commonsql.database;

import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.commonsql.util.TableCreator;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.oop.datamodule.commonsql.util.SqlUtil.escapeColumn;

@Getter
public abstract class SQLDatabase {
    private Map<String, Connection> threadConnections = new ConcurrentHashMap<>();

    protected abstract Connection provideConnection() throws Throwable;

    public abstract String getType();

    public abstract String columnEscaper();

    public synchronized Connection getConnection() {
        Connection connection = threadConnections.get(Thread.currentThread().getName());
        try {
            if (connection == null || connection.isClosed()) {
                connection = provideConnection();
                threadConnections.put(Thread.currentThread().getName(), connection);
            }
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to get connection. Perhaps incorrect params?", throwable);
        }

        return connection;
    }

    public List<String> getColumns(String table) {
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

    public void execute(String sql) {
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

    public boolean isPrimaryKeyUsed(String table, String[] structure, String primaryKey) {
        if (!primaryKey.endsWith("\"") && !primaryKey.startsWith("\""))
            primaryKey = "\"" + primaryKey + "\"";

        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT " + escapeColumn(structure[0], this) + " from " + table + " where " + escapeColumn(structure[0], this) + " = '" + primaryKey + "'")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getObject(1) != null;
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to check if table contains value (table=" + table + ", " + structure[0] + "=" + primaryKey + ") cause of " + e.getMessage(), e);
        }
    }

    @SneakyThrows
    public List<List<DataPair<String, String>>> getAllValuesOf(String table, String[] structure) {
        List<List<DataPair<String, String>>> allData = new LinkedList<>();
        try (ResultSet rs = getConnection().createStatement().executeQuery("SELECT * FROM " + table)) {
            while (rs.next()) {
                int i = 1;
                List<DataPair<String, String>> data = new LinkedList<>();
                for (String column : structure) {
                    data.add(new DataPair<>(rs.getMetaData().getColumnName(i), rs.getString(i)));
                    i++;
                }
                allData.add(data);
            }
        }
        return allData;
    }

    @SneakyThrows
    public void remove(String table, String[] structure, String primaryKey) {
        if (!primaryKey.startsWith("\""))
            primaryKey = "\"" + primaryKey + "\"";

        getConnection().createStatement().execute("DELETE FROM " + table + " WHERE " + escapeColumn(structure[0], this) + " = '" + primaryKey + "'");
    }

    public abstract void renameColumn(String table, DataPair<String, String>... pairs);

    public abstract void dropColumn(String table, String... columns);

    @SneakyThrows
    public void shutdown() {
        for (Connection value : getThreadConnections().values()) {
            value.close();
        }
    }
}
