package com.oop.datamodule.mongodb;

import com.google.gson.*;
import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.oop.datamodule.api.SerializedData;
import org.bson.*;

import java.util.Map;
import java.util.regex.Pattern;

public class MongoHelper {
  private static final Pattern QUOTE_REGEX = Pattern.compile("\"");
  private static final Function<String, String> replacer =
      in -> {
        if (in.startsWith("\"")) in = in.substring(1);

        if (in.endsWith("\"")) in = in.substring(0, in.length() - 1);

        return in;
      };

  public static MongoCollection<Document> getOrCreateCollection(
      MongoDatabase database, String collection) {
    try {
      return database.getCollection(collection);
    } catch (IllegalArgumentException throwable) {
      database.createCollection(collection);
      return getOrCreateCollection(database, collection);
    }
  }

  public static Document fromSerializedData(SerializedData data) {
    JsonElement jsonElement = data.getJsonElement();
    if (!jsonElement.isJsonObject())
      throw new IllegalArgumentException("Document can only be parsed from JsonObject!");

    Document document = new Document();
    JsonObject asJsonObject = data.getJsonElement().getAsJsonObject();
    for (Map.Entry<String, JsonElement> jsonElementEntry : asJsonObject.entrySet())
      appendBson(document, jsonElementEntry.getKey(), jsonElementEntry.getValue());

    return document;
  }

  public static SerializedData fromDocument(Document document) {
    JsonObject jsonObject = new JsonObject();
    for (Map.Entry<String, BsonValue> element :
        document
            .toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry())
            .entrySet()) appendJson(jsonObject, element.getKey(), element.getValue());

    return new SerializedData(jsonObject);
  }

  public static void appendJson(JsonObject object, String key, BsonValue element) {
    if (element == null || element.isNull()) {
      object.add(key, JsonNull.INSTANCE);
      return;
    }

    if (isPrimitive(element)) {
      object.add(key, new JsonPrimitive(convertBsonPrimitiveToJson(element)));
      return;
    }

    object.add(key, bsonDeepConvert(element));
  }

  private static boolean isPrimitive(BsonValue element) {
    return element.isBoolean() || element.isNumber() || element.isString();
  }

  private static String convertBsonPrimitiveToJson(BsonValue value) {
    if (value.isNull()) return "null";

    if (value.isString()) return value.asString().getValue();

    if (value.isBoolean()) return value.asBoolean().getValue() ? "1" : "0";

    if (value.isNumber()) return value.asNumber().doubleValue() + "d";

    if (value.isObjectId()) return value.asObjectId().getValue().toString();

    throw new IllegalStateException(
        "Unsupported primitive by type: " + value.getClass().getSimpleName());
  }

  private static JsonElement bsonDeepConvert(BsonValue value) {
    if (value.isArray()) {
      JsonArray array = new JsonArray();

      BsonArray bsonValues = value.asArray();
      for (BsonValue bsonValue : bsonValues) array.add(bsonDeepConvert(bsonValue));

      return array;
    }

    if (value.isDocument()) {
      JsonObject object = new JsonObject();

      BsonDocument bsonDocument = value.asDocument();
      for (Map.Entry<String, BsonValue> element : bsonDocument.entrySet())
        object.add(element.getKey(), bsonDeepConvert(element.getValue()));

      return object;
    }

    return new JsonPrimitive(convertBsonPrimitiveToJson(value));
  }

  public static void appendBson(Document document, String key, JsonElement element) {
    if (element.isJsonNull()) document.append(key, BsonNull.VALUE);
    else if (element.isJsonPrimitive()) document.append(key, replacer.apply(element.toString()));
    else document.append(key, jsonDeepConvert(element));
  }

  private static BsonValue jsonDeepConvert(JsonElement element) {
    if (element.isJsonObject()) {
      BsonDocument document = new BsonDocument();
      for (Map.Entry<String, JsonElement> entrySet : element.getAsJsonObject().entrySet()) {
        JsonElement value = entrySet.getValue();
        BsonValue o = jsonDeepConvert(value);
        document.append(entrySet.getKey(), o);
      }
      return document;
    }

    if (element.isJsonArray()) {
      BsonArray bsonValues = new BsonArray();
      for (JsonElement jsonElement : element.getAsJsonArray())
        bsonValues.add(jsonDeepConvert(jsonElement));

      return bsonValues;
    }

    return new BsonString(replacer.apply(element.toString()));
  }
}
