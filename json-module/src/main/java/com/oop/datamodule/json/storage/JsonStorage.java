package com.oop.datamodule.json.storage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.model.ModelBody;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.api.util.job.Job;
import com.oop.datamodule.api.util.job.JobsResult;
import com.oop.datamodule.api.util.job.JobsRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class JsonStorage<T extends ModelBody> extends Storage<T> {
  private static final Gson prettifiedGson;

  static {
    prettifiedGson = StorageInitializer.getInstance().getPrettyfiedGson();
  }

  private final File directory;

  public JsonStorage(StorageRegistry storageRegistry, File directory, boolean register) {
    super(storageRegistry, register);
    this.directory = directory;

    if (!directory.exists()) directory.mkdirs();
  }

  public JsonStorage(StorageRegistry storageRegistry, File directory) {
    this(storageRegistry, directory, true);
  }

  public JsonStorage(File directory) {
    this(null, directory, true);
  }

  public static Gson getPrettifiedGson() {
    return prettifiedGson;
  }

  @Override
  public void shutdown() {}

  @Override
  public void save(T object, boolean async, Runnable callback) {
    // Make sure we have an variant for this class
    findVariantNameFor(object.getClass());

    JsonModelLock<T> lock = getLock(object);
    if (lock.isLocked()) return;

    Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);
    runner.accept(
        () -> {
          lock.save();
          if (callback != null) callback.run();
        });
  }

  @Override
  public void remove(T object) {
    super.remove(object);

    acquireAndLaterRemove(
        object,
        () -> {
          getLock(object)
              .lockAndUseSelf(
                  lock -> {
                    File file = ((JsonModelLock) lock).getFile();
                    file.delete();
                  });
        });
  }

  @Override
  public void load(boolean async, Runnable callback) {
    Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);
    runner.accept(
        () -> {
          Set<DataPair<T, File>> loadedData = ConcurrentHashMap.newKeySet();

          JobsRunner acquire = JobsRunner.acquire();
          for (String s : getVariants().keySet()) {
            File variantDirectory = new File(directory + "/" + s);
            if (!variantDirectory.exists()) variantDirectory.mkdirs();

            for (File file : Objects.requireNonNull(variantDirectory.listFiles())) {
              acquire.addJob(
                  new Job() {
                    @Override
                    public String getName() {
                      return file.getName();
                    }

                    @Override
                    public void run() {
                      try {
                        BufferedReader reader =
                            new BufferedReader(
                                new InputStreamReader(
                                    new FileInputStream(file), StandardCharsets.UTF_8));
                        JsonObject jsonObject = prettifiedGson.fromJson(reader, JsonObject.class);
                        reader.close();
                        if (jsonObject == null) return;

                        SerializedData data = new SerializedData(jsonObject);
                        Optional<SerializedData> type = data.getChildren(getTypeVar());
                        if (!type.isPresent())
                          throw new IllegalAccessException(
                              "Failed to find type in serialized data. Data is outdated!");

                        Class<? extends T> clazz = getVariants().get(type.get().applyAs());
                        T object =
                            construct(
                                Objects.requireNonNull(
                                    clazz,
                                    "Failed to find clazz for serialized type: "
                                        + type.get().applyAs()));
                        object.deserialize(data);

                        loadedData.add(new DataPair<>(object, file));
                        loadObjectCache(object.getKey(), data);
                      } catch (Throwable throwable) {
                        throw new IllegalStateException(
                            "Failed to load object at file: "
                                + file.getParentFile().getName()
                                + "/"
                                + file.getName(),
                            throwable);
                      }
                    }
                  });
            }
          }

          JobsResult jobsResult = acquire.startAndWait();
          for (Throwable error : jobsResult.getErrors())
            StorageInitializer.getInstance().getErrorHandler().accept(error);

          for (DataPair<T, File> pair : loadedData) onAdd(pair.getKey());

          // On load
          getOnLoad().forEach(c -> c.accept(this));

          if (callback != null) callback.run();
        });
  }

  @Override
  public void save(boolean async, Runnable callback) {
    Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);
    runner.accept(
        () -> {
          for (T object : this) getLock(object).save();

          if (callback != null) callback.run();
        });
  }

  public String getTypeVar() {
    return "%%TYPE%%";
  }

  @Override
  protected JsonModelLock<T> getLock(T object) {
    return (JsonModelLock<T>)
        getLockMap()
            .computeIfAbsent(
                object.getKey(),
                k -> {
                  File variantDirectory =
                      new File(directory + "/" + findVariantNameFor(object.getClass()));
                  if (!variantDirectory.exists()) variantDirectory.mkdirs();

                  return new JsonModelLock<>(
                      object, new File(variantDirectory, object.getKey() + ".json"), this);
                });
  }
}
