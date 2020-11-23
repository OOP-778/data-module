package com.oop.datamodule.mongodb;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.Function;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
import org.bson.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MongoHelper {
    private static final Pattern QUOTE_REGEX = Pattern.compile("\"");
    private static final Function<String, String> replacer = in -> {
        Matcher matcher = QUOTE_REGEX.matcher(in);
        return matcher.replaceAll("");
    };

    public static MongoCollection<Document> getOrCreateCollection(MongoDatabase database, String collection) {
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

        String gsonData = StorageInitializer.getInstance().getGson().toJson(jsonElement);
        return Document.parse(gsonData);
    }

    public static SerializedData fromDocument(Document document) {
        JsonObject jsonObject = StorageInitializer.getInstance().getGson().fromJson(document.toJson(), JsonObject.class);
        return new SerializedData(jsonObject);
    }

    public static void append(Document document, String key, JsonElement element) {
        if (element.isJsonNull())
            document.append(key, BsonNull.VALUE);

        else if (element.isJsonPrimitive())
            document.append(key, replacer.apply(element.toString()));

        else
            document.append(key, deepConvert(element));
    }

    private static BsonValue deepConvert(JsonElement element) {
        if (element.isJsonObject()) {
            BsonDocument document = new BsonDocument();
            for (Map.Entry<String, JsonElement> entrySet : element.getAsJsonObject().entrySet()) {
                JsonElement value = entrySet.getValue();
                BsonValue o = deepConvert(value);
                document.append(entrySet.getKey(), o);
            }
            return document;
        }

        if (element.isJsonArray()) {
            BsonArray bsonValues = new BsonArray();
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                bsonValues.add(deepConvert(jsonElement));
            }
            return bsonValues;
        }

        return new BsonString(replacer.apply(element.toString()));
    }
}
