package com.oop.datamodule.commonsql.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.model.ModelCachedData;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.api.storage.lock.ModelLock;
import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.api.util.job.JobsResult;
import com.oop.datamodule.api.util.job.JobsRunner;
import com.oop.datamodule.commonsql.database.SQLDatabase;
import com.oop.datamodule.commonsql.model.SqlModelBody;
import com.oop.datamodule.commonsql.util.Column;
import com.oop.datamodule.commonsql.util.TableCreator;
import com.oop.datamodule.commonsql.util.TableEditor;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.oop.datamodule.commonsql.util.SqlUtil.escapeColumn;

public abstract class SqlStorage<T extends SqlModelBody> extends Storage<T> {
  private final Set<String> preparedTables = ConcurrentHashMap.newKeySet();

  @Getter private final SQLDatabase database;

  public SqlStorage(StorageRegistry storageRegistry, SQLDatabase database, boolean register) {
    super(storageRegistry, register);
    this.database = database;
  }

  public SqlStorage(StorageRegistry storageRegistry, SQLDatabase database) {
    this(storageRegistry, database, true);
  }

  public SqlStorage(SQLDatabase database) {
    this(null, database, true);
  }

  public abstract Stream<T> stream();

  @Override
  public void remove(T object) {
    acquireAndLaterRemove(
        object,
        () ->
            getLock(object)
                .lockAndUse(
                    () -> {
                      onRemove(object);
                      StorageInitializer.getInstance()
                          .getRunner(true)
                          .accept(
                              () ->
                                  database.remove(
                                      findVariantNameFor(object.getClass()),
                                      object.getStructure(),
                                      object.getKey()));
                    }));
  }

  @Override
  public void save(boolean async, Runnable callback) {
    Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);
    runner.accept(
        () -> {
          JobsRunner acquire = JobsRunner.acquire();

          database
              .getConnection()
              .useNonLock(
                  conn -> {
                    try {
                      for (T object : this) {
                        acquire.addJob(
                            new SqlJob(
                                () -> {
                                  ModelLock<T> lock = getLock(object);
                                  if (lock.isLocked()) return;

                                  lock.lockAndUse(
                                      () -> {
                                        try {
                                          if (!preparedTables.contains(
                                              findVariantNameFor(object.getClass())))
                                            prepareTable(object);

                                          SerializedData data = new SerializedData();
                                          object.serialize(data);

                                          JsonObject jsonObject =
                                              data.getJsonElement().getAsJsonObject();
                                          JsonElement[] jsonElements =
                                              new JsonElement[object.getStructure().length];
                                          for (int i = 0; i < object.getStructure().length; i++) {
                                            JsonElement element =
                                                jsonObject.get(object.getStructure()[i]);
                                            jsonElements[i] =
                                                Objects.requireNonNull(
                                                    element,
                                                    "Failed to find '"
                                                        + object.getStructure()[i]
                                                        + "' field inside serialized data of "
                                                        + object.getClass().getName());
                                          }

                                          String primaryKey = object.getKey();
                                          if (database.isPrimaryKeyUsed(
                                              findVariantNameFor(object.getClass()),
                                              object.getStructure(),
                                              primaryKey))
                                            updateObject(object, primaryKey, jsonObject);
                                          else insertObject(object, primaryKey, jsonObject);
                                        } catch (Throwable throwable) {
                                          handleError(
                                              new IllegalStateException(
                                                  "Failed to save object of identifier: "
                                                      + object.getKey(),
                                                  throwable));
                                        }
                                      });
                                }));
                      }
                    } catch (Throwable throwable) {
                        handleError(
                                new IllegalStateException(
                                        "Failed to save storage",
                                        throwable));
                    }

                    JobsResult jobsResult = acquire.startAndWait();
                    for (Throwable error : jobsResult.getErrors()) {
                      error.printStackTrace();
                    }

                    if (callback != null) callback.run();
                  });
        });
  }

  @Override
  public void shutdown() {
    getDatabase().shutdown();
  }

  @Override
  public void load(boolean async, Runnable callback) {
    Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);

    runner.accept(
        () -> {
          JobsRunner acquire = JobsRunner.acquire();
          for (Class<? extends T> clazz : getVariants().values()) {
            try {
              T dummy = construct(clazz);
              prepareTable(dummy);

              List<List<DataPair<String, String>>> allValues =
                  database.getAllValuesOf(
                      findVariantNameFor(dummy.getClass()), dummy.getStructure());
              for (List<DataPair<String, String>> allValue : allValues) {
                acquire.addJob(
                    new SqlJob(
                        () -> {
                          JsonObject object = toJson(allValue);
                          SerializedData data = new SerializedData(object);
                          try {

                            T t = construct(clazz);
                            t.deserialize(data);

                            onAdd(t);
                            loadObjectCache(t.getKey(), data);
                          } catch (Throwable exception) {
                            handleError(
                                new IllegalStateException(
                                    "Failed to deserialize "
                                        + clazz
                                        + " type with data: "
                                        + data.getJsonElement(),
                                    exception));
                          }
                        }));
              }
            } catch (Throwable throwable) {
              StorageInitializer.getInstance().getErrorHandler().accept(throwable);
            }
          }

          JobsResult jobsResult = acquire.startAndWait();
          for (Throwable error : jobsResult.getErrors())
            StorageInitializer.getInstance().getErrorHandler().accept(error);

          if (callback != null) callback.run();

          // On load
          getOnLoad().forEach(c -> c.accept(this));
        });
  }

  private JsonObject toJson(List<DataPair<String, String>> objectValues) {
    JsonObject object = new JsonObject();
    for (DataPair<String, String> objectValue : objectValues) {
      object.add(
          objectValue.getKey(),
          StorageInitializer.getInstance()
              .getGson()
              .fromJson(objectValue.getValue(), JsonElement.class));
    }
    return object;
  }

  public void save(T object) {
    save(object, true);
  }

  public void save(T object, boolean async) {
    save(object, async, null);
  }

  public void save(T object, Runnable callback) {
    save(object, true, callback);
  }

  public synchronized void save(T object, boolean async, Runnable callback) {
    Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);
    runner.accept(
        () -> {
          ModelLock<T> lock = getLock(object);
          if (lock.isLocked()) return;

          lock.lockAndUse(
              () -> {
                prepareTable(object);

                SerializedData data = new SerializedData();
                object.serialize(data);

                JsonObject jsonObject = data.getJsonElement().getAsJsonObject();
                for (int i = 0; i < object.getStructure().length; i++) {
                  JsonElement element = jsonObject.get(object.getStructure()[i]);
                  Objects.requireNonNull(
                      element,
                      "Failed to find '"
                          + object.getStructure()[i]
                          + "' field inside serialized data of "
                          + object.getClass().getName());
                }

                String primaryKey = object.getKey();
                if (database.isPrimaryKeyUsed(
                    findVariantNameFor(object.getClass()), object.getStructure(), primaryKey))
                  updateObject(object, primaryKey, jsonObject);
                else insertObject(object, primaryKey, jsonObject);

                if (callback != null) callback.run();
              });
        });
  }

  private synchronized void insertObject(T object, String primaryKey, JsonObject jsonObject) {
    ModelCachedData cache = new ModelCachedData();
    getDataCache().put(primaryKey, cache);

    getDatabase()
        .getConnection()
        .use(
            conn -> {
              try (PreparedStatement stmt =
                  conn.prepareStatement(
                      createInsertStatement(
                          findVariantNameFor(object.getClass()), object.getStructure()))) {
                for (int i = 0; i < object.getStructure().length; i++) {
                  String column = object.getStructure()[i];
                  String serializedData = jsonObject.get(column).toString();
                  cache.add(column, serializedData);

                  stmt.setString(i + 1, serializedData);
                }
                stmt.executeUpdate();
              } catch (SQLException e) {
                e.printStackTrace();
              }
            })
        .evict();
  }

  @SneakyThrows
  private String createInsertStatement(String tableName, String[] structure) {
    StringBuilder builder = new StringBuilder();
    builder
        .append("INSERT INTO ")
        .append(tableName)
        .append(" (")
        .append(
            Arrays.stream(structure)
                .map(column -> escapeColumn(column, database))
                .collect(Collectors.joining(",")))
        .append(") VALUES (");
    builder.append(Arrays.stream(structure).map(s -> "?").collect(Collectors.joining(",")));
    builder.append(")");
    return builder.toString();
  }

  private void updateObject(T object, String primaryKey, JsonObject jsonObject) {
    ModelCachedData cache =
        getDataCache().computeIfAbsent(primaryKey, key -> new ModelCachedData());
    String[] structure = object.getStructure();

    // Column, Data
    List<DataPair<String, String>> needsUpdate = new ArrayList<>();
    for (String struct : structure) {
      JsonElement element = jsonObject.get(struct);
      String serializedData = element.toString();
      if (!cache.isUpdated(struct, serializedData)) continue;

      needsUpdate.add(new DataPair<>(struct, serializedData));
    }
    if (needsUpdate.isEmpty()) return;

    getDatabase()
        .getConnection()
        .use(
            conn -> {
              try (PreparedStatement stmt =
                  conn.prepareStatement(
                      createUpdateStatement(
                          findVariantNameFor(object.getClass()),
                          object.getStructure()[0],
                          needsUpdate.stream()
                              .map(DataPair::getKey)
                              .collect(Collectors.toList())))) {
                int currentIndex = 1;

                for (DataPair<String, String> dataPair : needsUpdate) {
                  stmt.setString(currentIndex, dataPair.getValue());
                  currentIndex++;
                }

                stmt.setString(
                    currentIndex,
                    !primaryKey.startsWith("\"") ? "\"" + primaryKey + "\"" : primaryKey);
                stmt.executeUpdate();
              } catch (SQLException e) {
                e.printStackTrace();
              }
            })
        .evict();
  }

  @SneakyThrows
  protected String createUpdateStatement(String table, String pkColumn, List<String> columns) {
    StringBuilder builder = new StringBuilder();
    builder.append("UPDATE ").append(table).append(" SET ");

    boolean first = true;
    for (String column : columns) {
      if (!first) {
        builder.append(", ");

      } else first = false;
      builder.append(escapeColumn(column, database)).append(" = ?");
    }

    builder.append(" WHERE ").append(pkColumn).append(" = ?");
    return builder.toString();
  }

  protected void prepareTable(T object) {
    if (preparedTables.contains(findVariantNameFor(object.getClass()))) return;

    String[] structure = object.getStructure();
    String tableName = findVariantNameFor(object.getClass());

    if (database.getTables().contains(tableName)) updateTable(tableName, structure, database);
    else insertTable(tableName, structure, database);
  }

  protected void insertTable(String tableName, String[] structure, SQLDatabase database) {
    if (preparedTables.contains(tableName)) return;
    preparedTables.add(tableName);

    TableCreator tableCreator =
        database.newTableCreator().setName(tableName).primaryKey(structure[0], Column.VARCHAR);

    for (String column : Arrays.copyOfRange(structure, 1, structure.length))
      tableCreator.addColumn(column, Column.TEXT);

    tableCreator.create();
  }

  protected void updateTable(String tableName, String[] structure, SQLDatabase database) {
    if (preparedTables.contains(tableName)
        || structure.length == database.getColumns(tableName).size()) return;
    preparedTables.add(tableName);

    List<String> columns = database.getColumns(tableName);
    TableEditor editor = new TableEditor(tableName);

    Arrays.stream(structure)
        .filter(column -> !columns.contains(column))
        .forEach(column -> editor.addColumn(column, Column.TEXT.getSql()));

    editor.edit(database);
  }

  public synchronized void purge() {
    for (String preparedClass : preparedTables)
      getDatabase().execute("DROP TABLE " + preparedClass);

    preparedTables.clear();
  }
}
