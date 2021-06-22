package com.oop.datamodule.mongodb.handler;

import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.database.DatabaseHandler;
import com.oop.datamodule.mongodb.MongoHelper;
import com.oop.datamodule.mongodb.structure.MongoDatabaseStructure;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.mongodb.client.model.Projections.include;

@RequiredArgsConstructor
public class MongoDBHandler implements DatabaseHandler<MongoDatabaseStructure, MongoDBHandler> {

  private final MongoDatabase database;

  @Override
  public void remove(@NonNull MongoDatabaseStructure structure, @NonNull ObjectIdentifier data) {
    consumeCollection(
        structure.getCollectionName(),
        (collection, $) ->
            collection.findOneAndDelete(
                Filters.eq(
                    data.getKeyIdentifier(), MongoHelper.valueOf(data.getObjectThatIdentifies()))));
  }

  @Override
  public void updateStructure(@NonNull MongoDatabaseStructure structure) {
    consumeCollection(
        structure.getCollectionName(),
        (collection, isNew) -> {
          if (structure.getUniqueFieldName() != null && isNew) {
            Bson indexKey = Indexes.hashed(structure.getUniqueFieldName());
            ListIndexesIterable<Document> indexes = collection.listIndexes();

            for (Document document : indexes) {
              if (document.equals(indexKey)) return;
            }

            collection.createIndex(indexKey);
          }
        });
  }

  @Override
  public SerializedData grabData(
      @NonNull MongoDatabaseStructure structure, @NonNull GrabData data) {
    updateStructure(structure);

    return consumeCollectionAndReturn(
        structure.getCollectionName(),
        (collection, $) -> {
          Document first =
              collection
                  .find(
                      Filters.and(
                          Filters.eq(
                              data.getKeyIdentifier(),
                              MongoHelper.valueOf(data.getObjectThatIdentifies())),
                          include(data.getGrabbing())))
                  .limit(1)
                  .first();
          if (first == null) return new SerializedData();

          return MongoHelper.fromDocument(first);
        });
  }

  @Override
  public void updateOrInsertData(
      @NonNull MongoDatabaseStructure structure, @NonNull UpdateData data) {
    updateStructure(structure);

    consumeCollection(
        structure.getCollectionName(),
        (collection, $) -> {
          Bson filter =
              Filters.eq(
                  data.getKeyIdentifier(), MongoHelper.valueOf(data.getObjectThatIdentifies()));
          Document first = collection.find(filter).limit(1).first();
          if (first == null) {
            Document updatedFields = new Document();

            data.getUpdatingColumns()
                .forEach(
                    (key, value) ->
                        MongoHelper.appendBson(updatedFields, key, value.getJsonElement()));

            // Finally update fields
            collection.updateOne(
                filter, new Document("$set", updatedFields), new UpdateOptions().upsert(true));
            return;
          }

          SerializedData oneObject = new SerializedData();
          data.getUpdatingColumns()
              .forEach(
                  (key, sd) ->
                      oneObject.getJsonElement().getAsJsonObject().add(key, sd.getJsonElement()));
          collection.insertOne(MongoHelper.fromSerializedData(oneObject));
        });
  }

  @Override
  public boolean exists(@NonNull MongoDatabaseStructure structure, @NonNull ObjectIdentifier data) {
    return consumeCollectionAndReturn(
        structure.getCollectionName(),
        (collection, $) ->
            collection
                    .find(
                        Filters.eq(
                            data.getKeyIdentifier(),
                            MongoHelper.valueOf(data.getObjectThatIdentifies())))
                    .limit(1)
                    .first()
                != null);
  }

  @Override
  public boolean supportsPartialUpdates() {
    return true;
  }

  protected <T> T consumeCollectionAndReturn(
      @NonNull String name, @NonNull BiFunction<MongoCollection<Document>, Boolean, T> function) {
    try {
      return function.apply(database.getCollection(name), false);
    } catch (IllegalArgumentException throwable) {
      database.createCollection(name);
      return function.apply(database.getCollection(name), true);
    }
  }

  protected void consumeCollection(
      @NonNull String name,
      @NonNull BiConsumer<MongoCollection<Document>, Boolean> collectionConsumer) {
    try {
      collectionConsumer.accept(database.getCollection(name), false);
    } catch (IllegalArgumentException throwable) {
      database.createCollection(name);
      collectionConsumer.accept(database.getCollection(name), true);
    }
  }
}
