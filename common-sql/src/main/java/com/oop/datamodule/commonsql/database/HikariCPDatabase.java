package com.oop.datamodule.commonsql.database;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;

public abstract class HikariCPDatabase extends SQLDatabase {
    @Setter(AccessLevel.PROTECTED)
    @NonNull
    private HikariDataSource dbSource;

    @SneakyThrows
    public HikariCPDatabase() {
    }

    @Override
    @SneakyThrows
    protected Connection provideConnection() {
        return dbSource.getConnection();
    }

    @Override
    public void shutdown() {
        if (dbSource != null)
            dbSource.close();
    }

    @Override
    @SneakyThrows
    public void evict(Connection connection) {
        connection.close();
    }
}
