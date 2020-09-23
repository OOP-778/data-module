package com.oop.datamodule.storage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.StorageHolder;
import com.oop.datamodule.StorageInitializer;
import com.oop.datamodule.body.FlatDataBody;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Logger;

public abstract class SingleFileStorage<T extends FlatDataBody> extends FileStorage<T> {
    private static final Gson prettifiedGson;

    private static final Logger logger = Logger.getLogger("SingleFileStorage");

    static {
        prettifiedGson = StorageInitializer.getInstance().getPrettyfiedGson();
    }

    private File file;
    private ReentrantLock lock = new ReentrantLock();

    public SingleFileStorage(StorageHolder holder, File directory, String fileName) {
        super(holder);

        if (!directory.exists())
            directory.mkdirs();

        file = new File(directory, fileName.endsWith(".json") ? fileName : fileName + ".json");
    }

    public abstract Class<T> getClazz();

    @Override
    public boolean accepts(Class clazz) {
        return clazz.isAssignableFrom(getClazz());
    }

    @Override
    public void save(T object, boolean async, Runnable callback) {
        save(true, callback);
    }

    @Override
    public void remove(T object) {
        onRemove(object);
        save();
    }

    @Override
    public void load(boolean async, Runnable callback) {
        Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);
        runner.accept(() -> {
            lock.lock();
            try {
                makeSureFileExists();
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                JsonArray array = prettifiedGson.fromJson(reader, JsonArray.class);
                reader.close();
                if (array == null) return;

                for (JsonElement data : array) {
                    if (data.isJsonObject()) {
                        SerializedData serializedData = new SerializedData(data.getAsJsonObject());
                        Optional<SerializedData> type = serializedData.getChildren(getTypeVar());
                        Class<? extends T> clazz;
                        if (!type.isPresent()) {
                            logger.warning("Failed to find type in serialized data. Data is outdated. Using the first data type of the provided...");
                            clazz = getVariants().get(getVariants().keySet().stream().findFirst().orElse(null));
                        } else
                            clazz = getVariants().get(type.get().applyAs());

                        Constructor<? extends T> constructor = getConstructor(Objects.requireNonNull(clazz, "Failed to find clazz for serialized type: " + clazz.getSimpleName()));

                        T object = constructor.newInstance();
                        object.deserialize(serializedData);

                        onAdd(object);
                    }
                }

                if (callback != null)
                    callback.run();

                // On load
                getOnLoad().forEach(c -> c.accept(this));
            } catch (Throwable throwable) {
                if (lock.isHeldByCurrentThread())
                    lock.unlock();
                throw new IllegalStateException("Failed to load", throwable);

            } finally {
                if (lock.isHeldByCurrentThread())
                    lock.unlock();
            }
        });
    }

    @Override
    public void save(boolean async, Runnable callback) {
        Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);
        runner.accept(() -> {
            lock.lock();
            try {
                makeSureFileExists();
                JsonArray allSerializedData = new JsonArray();
                for (T object : this) {
                    SerializedData data = new SerializedData();
                    object.serialize(data);

                    data.write(getTypeVar(), object.getSerializedType());
                    allSerializedData.add(data.getJsonElement());
                }

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
                prettifiedGson.toJson(allSerializedData, writer);
                writer.close();

                if (callback != null)
                    callback.run();
            } catch (Throwable throwable) {
                if (lock.isHeldByCurrentThread())
                    lock.unlock();
                throw new IllegalStateException("Failed to save", throwable);

            } finally {
                if (lock.isHeldByCurrentThread())
                    lock.unlock();
            }
        });
    }

    private void makeSureFileExists() throws IOException {
        if (!file.exists())
            file.createNewFile();
    }
}
