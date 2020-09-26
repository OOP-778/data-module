package com.oop.datamodule.storage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.StorageHolder;
import com.oop.datamodule.StorageInitializer;
import com.oop.datamodule.body.FlatDataBody;
import com.oop.datamodule.util.DataPair;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public abstract class MultiFileStorage<T extends FlatDataBody> extends FileStorage<T> {
    private static final Gson prettifiedGson;

    static {
        prettifiedGson = StorageInitializer.getInstance().getPrettyfiedGson();
    }

    public Map<T, ObjectHandler<T>> handlers = new ConcurrentHashMap<>();
    private File directory;

    public MultiFileStorage(StorageHolder storageHolder, File directory) {
        super(storageHolder);
        this.directory = directory;

        if (!directory.exists())
            directory.mkdirs();
    }

    @Override
    public void save(T object, boolean async, Runnable callback) {
        ObjectHandler<T> handler = handlers.computeIfAbsent(object, key -> new ObjectHandler<>(object, new File(directory, object.getKey() + ".json")));
        Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);
        runner.accept(() -> {
            handler.save();
            if (callback != null)
                callback.run();
        });
    }

    @Override
    public void remove(T object) {
        super.remove(object);

        ObjectHandler<T> handler = handlers.get(object);
        if (handler == null) return;

        handlers.remove(object);
        if (handler.file.exists())
            handler.remove();
    }

    @Override
    public void load(boolean async, Runnable callback) {
        Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);
        runner.accept(() -> {
            Arrays
                    .stream(Objects.requireNonNull(directory.listFiles()))
                    .parallel()
                    .map(file -> {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                            JsonObject jsonObject = prettifiedGson.fromJson(reader, JsonObject.class);
                            reader.close();
                            if (jsonObject == null) return null;

                            SerializedData data = new SerializedData(jsonObject);
                            Optional<SerializedData> type = data.getChildren(getTypeVar());
                            if (!type.isPresent())
                                throw new IllegalAccessException("Failed to find type in serialized data. Data is outdated!");

                            Class<? extends T> clazz = getVariants().get(type.get().applyAs());
                            Constructor<? extends T> constructor = getConstructor(Objects.requireNonNull(clazz, "Failed to find clazz for serialized type: " + type.get().applyAs()));

                            T object = constructor.newInstance();
                            object.deserialize(data);

                            return new DataPair<>(object, file);
                        } catch (Throwable throwable) {
                            new IllegalStateException("Failed to load object at file: " + file.getParentFile().getName() + "/" + file.getName(), throwable).printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .forEach(pair -> {
                        handlers.put(pair.getKey(), new ObjectHandler<>(pair.getKey(), pair.getValue()));
                        onAdd(pair.getKey());
                    });

            if (callback != null)
                callback.run();

            // On load
            getOnLoad().forEach(c -> c.accept(this));
        });
    }

    @Override
    public void save(boolean async, Runnable callback) {
        Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);

        runner.accept(() -> {
            for (T object : this) {
                ObjectHandler<T> handler = handlers.computeIfAbsent(object, key -> new ObjectHandler<>(object, new File(directory, object.getKey() + ".json")));
                handler.save();
            }

            if (callback != null)
                callback.run();
        });
    }

    public static class ObjectHandler<T extends FlatDataBody> {
        private ReentrantLock lock = new ReentrantLock();
        private final T object;
        private File file;

        @SneakyThrows
        public ObjectHandler(@NonNull T object, @NonNull File file) {
            this.object = object;
            this.file = file;
            if (!file.exists())
                file.createNewFile();
        }

        public void save() {
            try {
                lock.lock();

                if (!file.exists())
                    file.createNewFile();

                SerializedData data = new SerializedData();
                object.serialize(data);

                data.write(getTypeVar(), object.getSerializedType());

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
                prettifiedGson.toJson(data.getJsonElement(), writer);
                writer.close();

            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to save object with id: " + object.getKey(), throwable);
            } finally {
                if (lock.isHeldByCurrentThread())
                    lock.unlock();
            }
        }

        public void remove() {
            try {
                lock.lock();
                file.delete();
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to remove object with id: " + object.getKey(), throwable);
            } finally {
                if (lock.isHeldByCurrentThread())
                    lock.unlock();
            }
        }
    }
}
