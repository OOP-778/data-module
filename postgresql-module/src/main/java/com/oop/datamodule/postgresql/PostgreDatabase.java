package com.oop.datamodule.postgresql;

import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.commonsql.database.SQLDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class PostgreDatabase extends SQLDatabase {
    private PostgreSQLCredential credential;
    private Connection connection;

    public PostgreDatabase(PostgreSQLCredential credential) {
        this.credential = credential;
    }

    @Override
    protected Connection provideConnection() throws Throwable {
        Class.forName("org.postgresql.Driver");
        if (connection == null || connection.isClosed())
            connection = DriverManager.getConnection(credential.toURL(), credential.username(), credential.password());

        return connection;
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
            execute("ALTER TABLE " + table + " CHANGE " + renamedColumn.getKey() + " \"" + renamedColumn.getValue() + "\" TEXT");
        }
    }
}
