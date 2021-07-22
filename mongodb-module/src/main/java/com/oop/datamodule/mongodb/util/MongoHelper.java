package com.oop.datamodule.mongodb.util;

import com.google.gson.*;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.oop.datamodule.api.SerializedData;
import org.bson.*;
import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.util.Map;

public class MongoHelper {
  public static MongoCollection<Document> getOrCreateCollection(
      MongoDatabase database, String collection) {
    try {
      return database.getCollection(collection);
    } catch (IllegalArgumentException throwable) {
      database.createCollection(collection);
      return getOrCreateCollection(database, collection);
    }
  }

  public static class BSON_TO_JSON {
    private static boolean isPrimitive(BsonValue element) {
      return element.isBoolean()
          || element.isNumber()
          || element.isString()
          || element.isObjectId()
          || element.isNull();
    }

    private static JsonElement convertPrimitive(BsonValue value) {
      if (value.isNull()) {
        return JsonNull.INSTANCE;
      }

      if (value.isBoolean()) {
        return new JsonPrimitive(value.asBoolean().getValue());
      }

      if (value.isString()) {
        return new JsonPrimitive(value.asString().getValue());
      }

      if (value.isObjectId()) {
        return new JsonPrimitive(value.asObjectId().getValue().toString());
      }

      if (value.isNumber()) {
        return convertNumber(value);
      }

      throw new IllegalStateException("Unknown primitive of " + value);
    }

    private static JsonPrimitive convertNumber(BsonValue value) {
      if (value.isInt32()) {
        return new JsonPrimitive(value.asNumber().intValue());
      }

      if (value.isInt64()) {
        return new JsonPrimitive(value.asNumber().longValue());
      }

      if (value.asNumber().isDecimal128()) {
        return new JsonPrimitive(value.asNumber().decimal128Value().bigDecimalValue());
      }

      if (value.asNumber().isDouble()) {
        return new JsonPrimitive(value.asNumber().doubleValue());
      }

      throw new IllegalStateException("Unknown number type of " + value);
    }

    static JsonElement convert(BsonValue value) {
      if (isPrimitive(value)) {
        return convertPrimitive(value);
      }

      if (value.isArray()) {
        JsonArray array = new JsonArray();

        BsonArray bsonValues = value.asArray();
        for (BsonValue bsonValue : bsonValues) array.add(convert(bsonValue));

        return array;
      }

      if (value.isDocument()) {
        JsonObject object = new JsonObject();

        BsonDocument bsonDocument = value.asDocument();
        for (Map.Entry<String, BsonValue> element : bsonDocument.entrySet())
          object.add(element.getKey(), convert(element.getValue()));

        return object;
      }

      throw new IllegalStateException("Unknown type of " + value);
    }

    public static SerializedData fromDocument(Document document) {
      JsonObject jsonObject = new JsonObject();
      for (Map.Entry<String, BsonValue> element :
          document
              .toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry())
              .entrySet()) jsonObject.add(element.getKey(), convert(element.getValue()));

      return new SerializedData(jsonObject);
    }
  }

  public static class JSON_TO_BSON {
    public static Document fromSerializedData(SerializedData data) {
      JsonElement jsonElement = data.getJsonElement();
      if (!jsonElement.isJsonObject())
        throw new IllegalArgumentException("Document can only be parsed from JsonObject!");

      return fromSerializedData(data);
    }

    protected static boolean isPrimitive(JsonElement element) {
      return element.isJsonPrimitive();
    }

    protected static BsonValue convertNumber(JsonPrimitive primitive) {
      final Number number = primitive.getAsNumber();
      if (number instanceof Double) {
        return new BsonDouble(number.doubleValue());
      }

      if (number instanceof Integer) {
        return new BsonInt32(number.intValue());
      }

      if (number instanceof Long) {
        return new BsonInt64(number.longValue());
      }

      if (number instanceof Float) {
        return new BsonDouble(number.doubleValue());
      }

      if (number instanceof BigDecimal) {
        return new BsonDecimal128(new Decimal128((BigDecimal) number));
      }

      throw new IllegalStateException("Unknown number of type " + primitive);
    }

    protected static BsonValue convertPrimitive(JsonElement element) {
      final JsonPrimitive primitive = element.getAsJsonPrimitive();

      if (primitive.isNumber()) {
        return convertNumber(primitive);
      }

      if (primitive.isBoolean()) {
        return new BsonBoolean(primitive.getAsBoolean());
      }

      if (primitive.isString()) {
        return new BsonString(primitive.getAsString());
      }

      throw new IllegalStateException("Unknown type of primitive for " + element);
    }

    protected static BsonValue convert(JsonElement element) {
      if (element.isJsonNull()) {
        return BsonNull.VALUE;
      }

      if (isPrimitive(element)) {
        return convertPrimitive(element);
      }

      if (element.isJsonObject()) {
        final BsonDocument document = new BsonDocument();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
          document.put(entry.getKey(), convert(entry.getValue()));
        }
        return document;
      }

      if (element.isJsonArray()) {
        final BsonArray array = new BsonArray();
        for (JsonElement jsonElement : element.getAsJsonArray()) {
          array.add(convert(jsonElement));
        }
      }

      throw new IllegalStateException("Unknown type for " + element);
    }
  }
}
