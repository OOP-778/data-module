package com.oop.datamodule.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.oop.datamodule.BodyCache;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.StorageHolder;
import com.oop.datamodule.StorageInitializer;
import com.oop.datamodule.body.SqlDataBody;
import com.oop.datamodule.database.Column;
import com.oop.datamodule.database.DatabaseWrapper;
import com.oop.datamodule.database.TableCreator;
import com.oop.datamodule.database.TableEditor;
import com.oop.datamodule.util.DataPair;
import com.oop.datamodule.util.DataUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SqlStorage<T extends SqlDataBody> extends Storage<T> {
    private final Set<Class> preparedClasses = new HashSet<>();
    
    @NonNull
    private final Map<String, BodyCache> dataCache = new ConcurrentHashMap<>();

    @Getter
    private DatabaseWrapper database;

    public SqlStorage(StorageHolder storageHolder, DatabaseWrapper database) {
        super(storageHolder);
        this.database = database;
    }

    public abstract Class<? extends T>[] getVariants();

    public abstract Stream<T> stream();

    @Override
    public boolean accepts(Class clazz) {
        return Arrays.asList(getVariants()).contains(clazz);
    }

    @Override
    public synchronized void remove(T object) {
        onRemove(object);
        StorageInitializer.getInstance().getRunner(true).accept(() -> database.remove(object.getTable(), object.getStructure(), object.getKey()));
    }

    @Override
    public synchronized void save(boolean async, Runnable callback) {
        Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);
        runner.accept(() -> {
            for (T object : this) {
                if (!preparedClasses.contains(object.getClass()))
                    prepareTable(object);

                SerializedData data = new SerializedData();
                object.serialize(data);

                JsonObject jsonObject = data.getJsonElement().getAsJsonObject();
                JsonElement[] jsonElements = new JsonElement[object.getStructure().length];
                for (int i = 0; i < object.getStructure().length; i++) {
                    JsonElement element = jsonObject.get(object.getStructure()[i]);
                    jsonElements[i] = Objects.requireNonNull(element, "Failed to find '" + object.getStructure()[i] + "' field inside serialized data of " + object.getClass().getName());
                }

                String primaryKey = object.getKey();
                if (database.isPrimaryKeyUsed(object.getTable(), object.getStructure(), primaryKey))
                    updateObject(object, primaryKey, jsonObject);

                else
                    insertObject(object, primaryKey, jsonObject);
            }

            if (callback != null)
                callback.run();
        });
    }

    @Override
    public synchronized void load(boolean async, Runnable callback) {
        Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);

        runner.accept(() -> {
            for (Class<? extends T> clazz : getVariants()) {
                try {
                    Constructor constructor = DataUtil.getConstructor(clazz);
                    T dummy = (T) constructor.newInstance();
                    prepareTable(dummy);

                    for (Set<DataPair<String, String>> dataPairs : database.getAllValuesOf(dummy.getTable(), dummy.getStructure())) {
                        JsonObject object = toJson(dataPairs);
                        SerializedData data = new SerializedData(object);

                        T t = (T) constructor.newInstance();
                        t.deserialize(data);

                        onAdd(t);
                    }

                    if (callback != null)
                        callback.run();

                    // On load
                    getOnLoad().forEach(c -> c.accept(this));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    private JsonObject toJson(Set<DataPair<String, String>> objectValues) {
        JsonObject object = new JsonObject();
        for (DataPair<String, String> objectValue : objectValues) {
            object.add(objectValue.getKey(), StorageInitializer.getInstance().getGson().fromJson(objectValue.getValue(), JsonElement.class));
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
        runner.accept(() -> {
            prepareTable(object);

            SerializedData data = new SerializedData();
            object.serialize(data);

            JsonObject jsonObject = data.getJsonElement().getAsJsonObject();
            JsonElement[] jsonElements = new JsonElement[object.getStructure().length];

            for (int i = 0; i < object.getStructure().length; i++) {
                JsonElement element = jsonObject.get(object.getStructure()[i]);
                jsonElements[i] = Objects.requireNonNull(element, "Failed to find '" + object.getStructure()[i] + "' field inside serialized data of " + object.getClass().getName());
            }

            String primaryKey = object.getKey();
            if (database.isPrimaryKeyUsed(object.getTable(), object.getStructure(), primaryKey))
                updateObject(object, primaryKey, jsonObject);

            else
                insertObject(object, primaryKey, jsonObject);

            if (callback != null)
                callback.run();
        });
    }

    private synchronized void insertObject(T object, String primaryKey, JsonObject jsonObject) {
        BodyCache cache = new BodyCache();
        dataCache.put(primaryKey, cache);

        try (PreparedStatement stmt = createInsertStatement(object.getTable(), object.getStructure())) {
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
    }

    @SneakyThrows
    private PreparedStatement createInsertStatement(String tableName, String[] structure) {
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ").append(tableName).append(" (").append(String.join(",", structure)).append(") VALUES (");
        builder.append(Arrays.stream(structure).map(s -> "?").collect(Collectors.joining(",")));
        builder.append(")");
        return database.getConnection().prepareStatement(builder.toString());
    }

    private synchronized void updateObject(T object, String primaryKey, JsonObject jsonObject) {
        BodyCache cache = dataCache.computeIfAbsent(primaryKey, key -> new BodyCache());
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

        try (PreparedStatement stmt = createUpdateStatement(object.getTable(), object.getStructure()[0], needsUpdate.stream().map(DataPair::getKey).collect(Collectors.toList()))) {
            int currentIndex = 1;

            for (DataPair<String, String> dataPair : needsUpdate) {
                stmt.setString(currentIndex, dataPair.getValue());
                currentIndex++;
            }

            if (!primaryKey.startsWith("\""))
                primaryKey = "\"" + primaryKey + "\"";

            stmt.setString(currentIndex, primaryKey);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    private PreparedStatement createUpdateStatement(String table, String pkColumn, List<String> columns) {
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ").append(table).append(" SET ");

        boolean first = true;
        for (String column : columns) {
            if (!first) {
                builder.append(", ");

            } else first = false;
            builder.append(column).append(" = ?");
        }

        builder.append(" WHERE ").append(pkColumn).append(" = ?");
        return database.getConnection().prepareStatement(builder.toString());
    }

    private synchronized void prepareTable(T object) {
        if (preparedClasses.contains(object.getClass())) return;

        String[] structure = object.getStructure();
        String tableName = object.getTable();

        if (database.getTables().contains(tableName))
            updateTable(tableName, structure, object, database);

        else
            insertTable(tableName, structure, object, database);
    }

    private synchronized void insertTable(String tableName, String[] structure, T object, DatabaseWrapper database) {
        if (preparedClasses.contains(object.getClass())) return;
        preparedClasses.add(object.getClass());

        TableCreator tableCreator = database
                .newTableCreator()
                .setName(tableName)
                .primaryKey(structure[0], Column.VARCHAR);

        for (String column : Arrays.copyOfRange(structure, 1, structure.length))
            tableCreator.addColumn(column, Column.TEXT);

        tableCreator.create();
    }

    private synchronized void updateTable(String tableName, String[] structure, T object, DatabaseWrapper database) {
        if (preparedClasses.contains(object.getClass()) || structure.length == database.getColumns(tableName).size())
            return;
        preparedClasses.add(object.getClass());

        List<String> columns = database.getColumns(tableName);
        TableEditor editor = new TableEditor(tableName);

        Arrays
                .stream(structure)
                .filter(column -> !columns.contains(column))
                .forEach(column -> editor.addColumn(column, Column.TEXT.getSql()));

        editor.edit(database);
    }
}
