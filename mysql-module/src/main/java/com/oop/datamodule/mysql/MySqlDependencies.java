package com.oop.datamodule.mysql;

import com.oop.datamodule.api.loader.Library;
import com.oop.datamodule.api.loader.StorageDependencies;

public class MySqlDependencies extends StorageDependencies {
  public MySqlDependencies() {
    addLib(Library.builder().from("com.zaxxer:HikariCP:4.0.1").build());
    addLib(Library.builder().from("mysql:mysql-connector-java:8.0.21").build());
  }
}
