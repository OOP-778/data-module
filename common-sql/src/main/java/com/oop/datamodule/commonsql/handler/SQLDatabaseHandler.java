package com.oop.datamodule.commonsql.handler;

import com.google.gson.JsonElement;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.database.DatabaseHandler;
import com.oop.datamodule.api.util.Preconditions;
import com.oop.datamodule.commonsql.database.SQLDatabase;
import com.oop.datamodule.commonsql.structure.SQLDatabaseStructure;
import com.oop.datamodule.commonsql.util.TableCreator;
import com.oop.datamodule.commonsql.util.TableEditor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

import static com.oop.datamodule.commonsql.util.SqlUtil.escapeColumn;
import static com.oop.datamodule.commonsql.util.SqlUtil.formatSQL;

@RequiredArgsConstructor
public class SQLDatabaseHandler
    implements DatabaseHandler<SQLDatabaseStructure, SQLDatabaseHandler> {

  @Getter private final SQLDatabase database;

  // Backup the database before doing an drastic change to the structure
  @Setter private boolean backupBeforeUpdateStructure = true;

  @Override
  public void remove(@NonNull SQLDatabaseStructure structure, @NonNull ObjectIdentifier data) {
    database.remove(
        structure.getTable(),
        data.getKeyIdentifier(),
        data.getObjectThatIdentifies().getJsonElement().toString());
  }

  @Override
  public void updateStructure(@NonNull SQLDatabaseStructure structure) {
    final List<String> currentTables = database.getTables();
    if (!currentTables.contains(structure.getTable())) {
      TableCreator tableCreator = database.newTableCreator().setName(structure.getPrimaryKey());

      structure.getColumnMap().forEach(tableCreator::addColumn);
      tableCreator.primaryKey(structure.getPrimaryKey());
      tableCreator.create();
    }

    // Now we need to find if we're missing tables or if we need to remove columns
    final List<String> currentColumns = database.getColumns(structure.getTable());
    final Set<String> memoryColumns = structure.getColumnMap().keySet();

    // No changes to the table has been done
    if (currentColumns.containsAll(memoryColumns)) {
      return;
    }

    // Changes have been done, let's find out what type of changes
    TableEditor editor = new TableEditor(structure.getTable());

    // Backup the table if needed
    if (backupBeforeUpdateStructure) {
      // TODO: Backup table
    }

    for (String memoryColumn : memoryColumns) {
      if (!currentColumns.contains(memoryColumn)) {
        editor.addColumn(memoryColumn, structure.getColumnMap().get(memoryColumn).getSql());
      }
    }

    for (String currentColumn : currentColumns) {
      if (!memoryColumns.contains(currentColumn)) {
        editor.addDropColumn(currentColumn);
      }
    }

    editor.edit(database);
  }

  @Override
  public SerializedData grabData(
      @NonNull SQLDatabaseStructure structure, @NonNull GrabData grabData) {
    // Update structure based cause of dynamic database structures
    updateStructure(structure);

    // If it doesn't exist just return empty serialized data
    if (!exists(structure, grabData)) return new SerializedData();

    return database.getValuesFromTable(
        structure.getTable(),
        new HashSet<>(Arrays.asList(grabData.getGrabbing())),
        structure.getPrimaryKey(),
        jsonElementToString(grabData.getObjectThatIdentifies()));
  }

  @Override
  public void updateOrInsertData(
      @NonNull SQLDatabaseStructure structure, @NonNull UpdateData updateData) {
    // Update structure based cause of dynamic database structures
    updateStructure(structure);

    if (!exists(structure, updateData)) {
      database.execute(createUpdateStatement(structure, updateData));
      return;
    }

    database.execute(createInsertStatement(structure, updateData));
  }

  @Override
  public boolean exists(@NonNull SQLDatabaseStructure structure, @NonNull ObjectIdentifier data) {
    return database.isPrimaryKeyUsed(
        structure.getTable(),
        data.getKeyIdentifier(),
        jsonElementToString(data.getObjectThatIdentifies()));
  }

  @Override
  public boolean supportsPartialUpdates() {
    return true;
  }

  protected String createUpdateStatement(
      @NonNull SQLDatabaseStructure structure, @NonNull UpdateData data) {
    return formatSQL(
        "UPDATE {} SET {} WHERE {} = {}",
        structure.getTable(),
        data.getUpdatingColumns().entrySet().stream()
            .map(
                column ->
                    formatSQL(
                        "{} = {}",
                        escapeColumn(column.getKey(), database),
                        jsonElementToString(column.getValue())))
            .collect(Collectors.joining(", ")),
        escapeColumn(data.getKeyIdentifier(), database),
        jsonElementToString(data.getObjectThatIdentifies()));
  }

  protected String createInsertStatement(
      @NonNull SQLDatabaseStructure structure, @NonNull UpdateData data) {
    final int columnsSize = data.getUpdatingColumns().size();
    final String[] keys = new String[columnsSize];
    final String[] values = new String[columnsSize];

    int index = 0;
    for (Map.Entry<String, SerializedData> updatingColumnEntry :
        data.getUpdatingColumns().entrySet()) {
      keys[index] = escapeColumn(updatingColumnEntry.getKey(), database);
      values[index] = jsonElementToString(updatingColumnEntry.getValue());
      index++;
    }

    return formatSQL(
        "INSERT INTO {} ({}) VALUES ({})",
        structure.getTable(),
        String.join(", ", keys),
        String.join(", ", values));
  }

  protected String jsonElementToString(SerializedData element) {
    return jsonElementToString(element.getJsonElement());
  }

  protected String jsonElementToString(JsonElement element) {
    String toReturn;
    Preconditions.checkArgument(!element.isJsonNull(), "Element cannot be null!");

    if (element.isJsonPrimitive()) {
      toReturn = element.getAsJsonPrimitive().getAsString();
    } else {
      toReturn = element.toString();
    }

    if (toReturn.startsWith("\"") && toReturn.endsWith("\"")) {
      toReturn = "\"" + toReturn + "\"";
    }

    return toReturn;
  }
}
