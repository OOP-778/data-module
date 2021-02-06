package com.oop.datamodule.mongodb.storage;

import static com.oop.datamodule.mongodb.MongoHelper.appendBson;
import static com.oop.datamodule.mongodb.MongoHelper.fromDocument;
import static com.oop.datamodule.mongodb.MongoHelper.fromSerializedData;
import static com.oop.datamodule.mongodb.MongoHelper.getOrCreateCollection;

import com.google.gson.JsonElement;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.model.ModelCachedData;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.api.storage.lock.ModelLock;
import com.oop.datamodule.api.util.job.JobsResult;
import com.oop.datamodule.api.util.job.JobsRunner;
import com.oop.datamodule.mongodb.MongoJob;
import com.oop.datamodule.mongodb.model.MongoModelBody;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.function.Consumer;
import lombok.NonNull;
import org.bson.Document;
import org.bson.conversions.Bson;

public abstract class MongoDBStorage<T extends MongoModelBody> extends Storage<T> {
  private final MongoDatabase database;

  public MongoDBStorage(
      StorageRegistry storageRegistry, @NonNull MongoDatabase database, boolean register) {
    super(storageRegistry, register);
    this.database = database;
  }

  public MongoDBStorage(StorageRegistry storageRegistry, @NonNull MongoDatabase database) {
    this(storageRegistry, database, true);
  }

  public MongoDBStorage(@NonNull MongoDatabase database) {
    this(null, database);
  }

  @Override
  public void shutdown() {}

  @Override
  public void save(T object, boolean async, Runnable callback) {
    Consumer<Runnable> runner = StorageInitializer.getInstance().getRunner(async);
    runner.accept(() -> _save(object));
  }

  protected void _save(T object) {
    String key = object.getKey();
    String table = findVariantNameFor(object.getClass());
    ModelLock<T> lock = getLock(object);
    if (lock.isLocked()) return;

    lock.lockAndUse(
        () -> {
          SerializedData data = serializeObject(object);
          Bson filter = Filters.eq(object.getIdentifierKey(), key);

          ModelCachedData modelCachedData =
              getDataCache().computeIfAbsent(key, k -> new ModelCachedData());

          MongoCollection<Document> collection = getOrCreateCollection(database, table);
          Document docObject = collection.find(filter).first();

          // If we have object with key already present
          if (docObject != null) {
            Document updatedFields = new Document();
            data.getJsonElement()
                .getAsJsonObject()
                .entrySet()
                .forEach(
                    entry -> {
                      JsonElement element = entry.getValue();
                      if (!modelCachedData.isUpdated(entry.getKey(), element.toString())) return;

                      appendBson(updatedFields, entry.getKey(), entry.getValue());
                    });
            if (updatedFields.isEmpty()) return;

            // Finally update fields
            collection.updateOne(
                filter, new Document("$set", updatedFields), new UpdateOptions().upsert(true));

          } else {
            Document document = fromSerializedData(data);
            document.append(object.getIdentifierKey(), key);
            collection.insertOne(document);

            loadObjectCache(key, data);
          }
        });
  }

  @Override
  public void remove(T object) {
    onRemove(object);
    StorageInitializer.getInstance()
        .getRunner(true)
        .accept(
            () ->
                acquireAndLaterRemove(
                    object,
                    () ->
                        getLock(object)
                            .lockAndUse(
                                () -> {
                                  MongoCollection<Document> collection =
                                      getOrCreateCollection(
                                          database, findVariantNameFor(object.getClass()));
                                  collection.findOneAndDelete(
                                      Filters.eq(object.getIdentifierKey(), object.getKey()));
                                })));
  }

  @Override
  public void load(boolean async, Runnable callback) {
    StorageInitializer.getInstance()
        .getRunner(async)
        .accept(
            () -> {
              JobsRunner acquire = JobsRunner.acquire();

              for (Map.Entry<String, Class<T>> variantEntry : getVariants().entrySet()) {
                String key = variantEntry.getKey();
                MongoCollection<Document> variantCollection = null;

                try {
                  variantCollection = database.getCollection(key);
                } catch (Throwable ignored) {
                }

                // Because collection doesn't exist, we can continue our loop
                if (variantCollection == null) continue;

                Constructor<T> constructor = getConstructor(variantEntry.getValue());

                try (MongoCursor<Document> cursor = variantCollection.find().iterator()) {
                  if (!cursor.hasNext()) continue;

                  Document next = cursor.next();
                  acquire.addJob(
                      new MongoJob(
                          () -> {
                            SerializedData data = fromDocument(next);
                            try {
                              T object = constructor.newInstance();
                              object.deserialize(data);

                              onAdd(object);
                              loadObjectCache(object.getKey(), data);
                            } catch (Throwable throwable) {
                              StorageInitializer.getInstance().getErrorHandler().accept(throwable);
                            }
                          }));
                }
              }

              JobsResult jobsResult = acquire.startAndWait();
              for (Throwable error : jobsResult.getErrors())
                StorageInitializer.getInstance().getErrorHandler().accept(error);

              // On load
              getOnLoad().forEach(c -> c.accept(this));

              if (callback != null) callback.run();
            });
  }

  @Override
  public void save(boolean async, Runnable callback) {
    StorageInitializer.getInstance()
        .getRunner(async)
        .accept(
            () -> {
              for (T object : this) _save(object);

              if (callback != null) callback.run();
            });
  }

  protected SerializedData serializeObject(T object) {
    SerializedData data = new SerializedData();
    object.serialize(data);
    return data;
  }

  protected MongoCollection<Document> getCollectionFor(T object) {
    String table = findVariantNameFor(object.getClass());
    return getOrCreateCollection(database, table);
  }
}
