package com.oop.datamodule.mysql;

import com.oop.datamodule.commonsql.database.SqlCredential;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Connection;

@Accessors(chain = true, fluent = true)
@Setter
@Getter
public class MySQLCredential implements SqlCredential {

    private String database;
    private String hostname;
    private int port = 3306;
    private String username;
    private String password;
    private boolean useSSL;

    private MySQLDatabase mySQLDatabase;

    public MySQLDatabase build() {
        if (mySQLDatabase != null)
            return mySQLDatabase;

        return new MySQLDatabase(this);
    }

    public String toURL() {
        String url = "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database;
        if (useSSL)
            url += "?useSSL=true";

        return url;
    }

    @Override
    public boolean test() {
        try {
            MySQLDatabase build = build();
            try (Connection connection = build.provideConnection()) {}
        } catch (Throwable throwable) {
            throw new IllegalStateException("MySQL database test failed...", throwable);
        }

        return true;
    }
}
