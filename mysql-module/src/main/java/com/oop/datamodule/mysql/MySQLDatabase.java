package com.oop.datamodule.mysql;

import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.commonsql.database.SQLDatabase;
import lombok.Getter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public class MySQLDatabase extends SQLDatabase {
    private final MySQLCredential credential;

    public MySQLDatabase(MySQLCredential props) {
        credential = props;
    }

    private Connection connection;

    @Override
    protected Connection provideConnection() throws SQLException {
        if (connection == null || connection.isClosed())
            connection = DriverManager.getConnection(credential.toURL(), credential.username(), credential.password());

        return connection;
    }

    @Override
    public List<String> getTables() {
        Connection connection = getConnection();
        List<String> tables = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT table_name FROM information_schema.tables WHERE table_type = 'base table'")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to get tables", throwable);
        }
        return tables;
    }

    @Override
    public void renameColumn(String table, DataPair<String, String>... pairs) {
        List<String> columns = getColumns(table);
        for (DataPair<String, String> renamedColumn : pairs) {
            if (!columns.contains(renamedColumn.getKey())) continue;
            execute("ALTER TABLE " + table + " CHANGE " + renamedColumn.getKey() + " " + renamedColumn.getValue() + " TEXT");
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
}
