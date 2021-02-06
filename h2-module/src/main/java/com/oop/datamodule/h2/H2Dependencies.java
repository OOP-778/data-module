package com.oop.datamodule.h2;

import com.oop.datamodule.api.loader.Library;
import com.oop.datamodule.api.loader.StorageDependencies;

public class H2Dependencies extends StorageDependencies {
  public H2Dependencies() {
    addLib(Library.builder().from("com.h2database:h2:1.4.200").build());
  }
}
