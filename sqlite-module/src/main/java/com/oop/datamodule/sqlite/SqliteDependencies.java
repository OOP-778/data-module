package com.oop.datamodule.sqlite;

import com.oop.datamodule.api.loader.Library;
import com.oop.datamodule.api.loader.StorageDependencies;

public class SqliteDependencies extends StorageDependencies {
  public SqliteDependencies() {
    addLib(Library.builder().from("org.xerial:sqlite-jdbc:3.32.3.2").build());
  }
}
