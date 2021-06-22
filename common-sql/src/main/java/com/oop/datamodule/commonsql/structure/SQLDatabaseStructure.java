package com.oop.datamodule.commonsql.structure;

import com.oop.datamodule.api.database.DatabaseStructure;
import com.oop.datamodule.commonsql.util.Column;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class SQLDatabaseStructure implements DatabaseStructure {

  private @NonNull final String table;
  private @NonNull final Map<String, Column> columnMap;
  private @NonNull final String primaryKey;

  public static class SQLDatabaseStructureBuilder {
    private final Map<String, Column> columnMap = new LinkedHashMap<>();

    public SQLDatabaseStructureBuilder appendColumn(String name, Column type) {
      columnMap.put(name, type);
      return this;
    }
  }
}
