package com.oop.datamodule.postgresql;

import com.oop.datamodule.api.loader.Library;
import com.oop.datamodule.api.loader.StorageDependencies;

public class PostgreSqlDependencies extends StorageDependencies {
  public PostgreSqlDependencies() {
    super();
    addLib(Library.builder().from("com.zaxxer:HikariCP:4.0.1").build());
    addLib(Library.builder().from("org.postgresql:postgresql:42.2.18.jre6").build());
  }
}
