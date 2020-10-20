package com.oop.datamodule;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.oop.datamodule.util.DataPair;
import com.oop.datamodule.util.DataUtil;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.oop.datamodule.util.DataUtil.wrap;

public class SerializedData {

    @Getter
    private final JsonElement jsonElement;

    public SerializedData() {
        this(new JsonObject());
    }

    public SerializedData(JsonElement jsonElement) {
        this.jsonElement = jsonElement;
    }

    public <T> T applyAs(Class<T> clazz, Supplier<T> def) {
        T ob = DataUtil.fromElement(jsonElement, clazz);
        return ob == null ? ob : def.get();
    }

    public <T> T applyAs(Class<T> clazz) {
        return DataUtil.fromElement(jsonElement, clazz);
    }

    public <T> T applyAs() {
        return (T) DataUtil.fromElement(jsonElement);
    }

    public Stream<SerializedData> applyAsCollection() {
        Preconditions.checkArgument(jsonElement.isJsonArray(), "Cannot convert non array object into collection!");
        JsonArray array = jsonElement.getAsJsonArray();
        return StreamSupport
                .stream(array.spliterator(), false)
                .map(SerializedData::new);
    }

    public Stream<DataPair<SerializedData, SerializedData>> applyAsMap() {
        Preconditions.checkArgument(jsonElement.isJsonArray(), "Cannot convert non array object into map!");
        List<JsonElement> elements = StreamSupport.stream(jsonElement.getAsJsonArray().spliterator(), false).collect(Collectors.toList());
        return elements.stream()
                .map(element -> {
                    JsonObject jsonObject = element.getAsJsonObject();
                    JsonElement key = jsonObject.get("key");
                    JsonElement value = jsonObject.get("value");
                    return new DataPair<>(new SerializedData(key), new SerializedData(value));
                });
    }

    public <T> T applyAs(String field, Class<T> clazz, Supplier<T> def) {
        return getElement(field).map(element -> DataUtil.fromElement(element, clazz)).orElseGet(def == null ? () -> null : def);
    }

    public boolean has(String field) {
        return funcJsonObject(jo -> jo.has(field) && jo.get(field) != JsonNull.INSTANCE, () -> false);
    }

    public Stream<SerializedData> applyAsCollection(String field) {
        JsonArray array = getElement(field).map(JsonElement::getAsJsonArray).orElse(new JsonArray());
        return StreamSupport
                .stream(array.spliterator(), false)
                .map(SerializedData::new);
    }

    public Stream<DataPair<SerializedData, SerializedData>> applyAsMap(String field) {
        JsonArray array = getElement(field).map(JsonElement::getAsJsonArray).orElse(new JsonArray());
        List<JsonElement> elements = StreamSupport.stream(array.spliterator(), false).collect(Collectors.toList());
        return elements.stream()
                .map(element -> {
                    JsonObject jsonObject = element.getAsJsonObject();
                    JsonElement key = jsonObject.get("key");
                    JsonElement value = jsonObject.get("value");
                    return new DataPair<>(new SerializedData(key), new SerializedData(value));
                });
    }

    public <T> T applyAs(String field, Class<T> clazz) {
        return applyAs(field, clazz, null);
    }

    public Optional<SerializedData> getChildren(String field) {
        return getElement(field)
                .map(SerializedData::new);
    }

    public Optional<JsonElement> getElement(String field) {
        Preconditions.checkArgument(jsonElement.isJsonObject(), "Failed to get element cause it's not an JsonObject");
        JsonElement element = jsonElement.getAsJsonObject().get(field);
        return (element == null || element == JsonNull.INSTANCE) ? Optional.empty() : Optional.of(element);
    }

    public void write(String field, Object object) {
        Preconditions.checkArgument(jsonElement.isJsonObject(), "You cannot set a field in JsonElement!");
        consumeJsonObject(jo -> {
            JsonElement wrap = wrap(object);
            jo.add(field, wrap);
        });
    }

    private void consumeJsonObject(Consumer<JsonObject> consumer) {
        if (jsonElement.isJsonObject())
            consumer.accept(jsonElement.getAsJsonObject());
    }

    public <T> T funcJsonObject(Function<JsonObject, T> function, Supplier<T> def) {
        if (jsonElement.isJsonObject())
            return function.apply(jsonElement.getAsJsonObject());

        return null;
    }

    public <T> T applyAs(String field) {
        return (T) getChildren(field).map(applyAs()).orElse(null);
    }
}
