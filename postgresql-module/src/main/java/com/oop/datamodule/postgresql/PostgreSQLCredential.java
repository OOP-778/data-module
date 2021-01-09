package com.oop.datamodule.postgresql;

import com.oop.datamodule.commonsql.database.SqlCredential;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Connection;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class PostgreSQLCredential implements SqlCredential {
    private String database;
    private String hostname;
    private int port = 5432;
    private String username;
    private String password;
    private boolean useSSL;
    private PostgreDatabase postgreDatabase;

    public PostgreDatabase build() {
        if (postgreDatabase != null)
            return postgreDatabase;

        return new PostgreDatabase(this);
    }

    public String toURL() {
        String url = "jdbc:postgresql://" + this.hostname + ":" + this.port + "/" + this.database;
        if (useSSL)
            url += "?useSSL=true";

        return url;
    }

    @Override
    public boolean test() {
        try {
            PostgreDatabase build = build();
            try (Connection connection = build.provideConnection()) {}
        } catch (Throwable throwable) {
            throw new IllegalStateException("Postgre database test failed...", throwable);
        }

        return true;
    }
}
