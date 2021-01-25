package com.oop.datamodule.postgresql;

import com.oop.datamodule.api.loader.Library;
import com.oop.datamodule.api.loader.StorageDependencies;

public class PostgreSqlDependencies extends StorageDependencies {
    public PostgreSqlDependencies() {
        addLib(Library.builder().from("org.postgresql:postgresql:42.2.18.jre6").build());
    }
}
