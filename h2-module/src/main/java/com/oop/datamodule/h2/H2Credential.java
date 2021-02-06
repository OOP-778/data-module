package com.oop.datamodule.h2;

import com.oop.datamodule.commonsql.database.SqlCredential;
import java.io.File;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
@Setter
public class H2Credential implements SqlCredential {
  private File folder;
  private String database;
  private H2Database h2Database;

  public H2Database build() {
    if (h2Database != null) return h2Database;

    return new H2Database(folder, database);
  }

  @Override
  public boolean test() {
    try {
      H2Database build = build();
      build.getConnection().use(conn -> {});
    } catch (Throwable throwable) {
      throw new IllegalStateException("H2 database test failed...", throwable);
    }

    return true;
  }
}
