package com.oop.datamodule.mysql;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@Setter
@Getter
public class MySQLCredential {

    private String database;
    private String hostname;
    private int port;
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
}
