package com.oop.datamodule.mongodb.handler;

import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.database.DatabaseHandler;
import com.oop.datamodule.api.database.DatabaseStructure;
import com.oop.datamodule.mongodb.util.MongoHelper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.mongodb.client.model.Projections.include;

@RequiredArgsConstructor
public class MongoDBHandler implements DatabaseHandler {

  private final MongoDatabase database;

  @Override
  public void remove(@NonNull DatabaseStructure structure, @NonNull ObjectIdentifier data) {
    consumeCollection(
        structure.modelName(), (collection, $) -> collection.findOneAndDelete(toFilter(data)));
  }

  @Override
  public void updateStructure(@NonNull DatabaseStructure structure) {
    consumeCollection(
        structure.modelName(),
        (collection, isNew) -> {
          ListIndexesIterable<Document> indexes = collection.listIndexes();
          PRIM_LOOP:
          for (String primaryKey : structure.primaryKeys()) {
            Bson indexKey = Indexes.hashed(primaryKey);
            for (Document document : indexes) {
              if (document.equals(indexKey)) continue PRIM_LOOP;
            }

            collection.createIndex(indexKey);
          }
        });
  }

  protected Bson toFilter(@NonNull ObjectIdentifier identifier) {
    return Filters.eq(
        identifier.getKeyIdentifier(),
        MongoHelper.JSON_TO_BSON.fromSerializedData(identifier.getObjectThatIdentifies()));
  }

  @Override
  public SerializedData grabData(@NonNull DatabaseStructure structure, @NonNull GrabData data) {
    updateStructure(structure);

    return consumeCollectionAndReturn(
        structure.modelName(),
        (collection, $) -> {
          Document first =
              collection
                  .find(Filters.and(toFilter(data), include(data.getGrabbing())))
                  .limit(1)
                  .first();
          if (first == null) return new SerializedData();

          return MongoHelper.BSON_TO_JSON.fromDocument(first);
        });
  }

  @Override
  public void updateOrInsertData(@NonNull DatabaseStructure structure, @NonNull UpdateData data) {
    updateStructure(structure);

    consumeCollection(
        structure.modelName(),
        (collection, $) -> {
          Bson filter = toFilter(data);
          Document first = collection.find(filter).limit(1).first();
          if (first == null) {
            Document updatedFields = new Document();

            data.getProperties()
                .forEach(
                    (key, value) ->
                        updatedFields.put(key, MongoHelper.JSON_TO_BSON.fromSerializedData(value)));

            // Finally update fields
            collection.updateOne(
                filter, new Document("$set", updatedFields), new UpdateOptions().upsert(true));
            return;
          }

          SerializedData oneObject = new SerializedData();
          data.getProperties()
              .forEach(
                  (key, sd) ->
                      oneObject.getJsonElement().getAsJsonObject().add(key, sd.getJsonElement()));
          collection.insertOne(MongoHelper.JSON_TO_BSON.fromSerializedData(oneObject));
        });
  }

  @Override
  public boolean exists(@NonNull DatabaseStructure structure, @NonNull ObjectIdentifier data) {
    return consumeCollectionAndReturn(
        structure.modelName(),
        (collection, $) -> collection.find(toFilter(data)).limit(1).first() != null);
  }

  @Override
  public boolean supportsPartialUpdates() {
    return true;
  }

  @Override
  public String identifier() {
    return "MongoDB";
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
