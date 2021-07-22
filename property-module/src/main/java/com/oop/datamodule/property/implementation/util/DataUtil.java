package com.oop.datamodule.property.implementation.util;

import com.google.gson.*;
import com.google.gson.internal.Primitives;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.property.implementation.PropertyController;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.util.*;

public class DataUtil {
  private static final List<Class> primitiveList =
      new ArrayList<Class>() {
        {
          add(Integer.class);
          add(String.class);
          add(Double.class);
          add(Long.class);
          add(Boolean.class);
          add(Float.class);
        }
      };

  public static Object unwrap(JsonElement element) {
    if (element.isJsonNull()) return null;

    if (element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isString()) {
        return primitive.getAsString();
      }

      if (primitive.isNumber()) {
        return primitive.getAsNumber();
      }

      if (primitive.isBoolean()) {
        return primitive.getAsBoolean();
      }
    }

    throw new IllegalStateException(
        String.format(
            "JsonElement by type %s cannot be converted back to Object!",
            element.getClass().getSimpleName()));
  }

  public static JsonElement wrap(Object object) {
    if (object == null) {
      return JsonNull.INSTANCE;
    }

    // If primitive just convert to string
    if (DataUtil.isPrimitive(object.getClass())) {
      return PropertyController.getInstance().getGson().toJsonTree(object);
    }

    // If implements SerializableObject, serialize
    if (object instanceof SerializableObject) {
      SerializedData serializedData = new SerializedData();
      ((SerializableObject) object).serialize(serializedData);

      return serializedData.getJsonElement();
    }

    // If is collection
    if (object instanceof Collection) {
      JsonArray array = new JsonArray();
      for (Object listObject : (Collection) object) array.add(wrap(listObject));

      return array;
    }

    // If is map
    if (object instanceof Map) {
      Map map = (Map) object;
      JsonArray array = new JsonArray();
      map.forEach(
          (key, value) -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("key", wrap(key));
            jsonObject.add("value", wrap(value));
            array.add(jsonObject);
          });
      return array;
    }

    // If it's an enum
    if (object.getClass().isEnum()) {
      return new JsonPrimitive(((Enum) object).name());
    }

    Optional<? extends StorageInitializer.AdapterObject> adapter =
        StorageInitializer.getInstance().findAdapter(object.getClass());
    if (adapter.isPresent()) {
      return adapter.get().getAdapter().toJsonTree(object);
    }

    return StorageInitializer.getInstance().getGson().toJsonTree(object);
  }

  public static <T> T fromElement(JsonElement element, Class<T> clazz) {
    clazz = Primitives.wrap(clazz);
    if (SerializableObject.class.isAssignableFrom(clazz)) {
      return fromSerializable(element, clazz);
    }

    if (clazz.isEnum()) {
      return (T) Enum.valueOf((Class<Enum>) clazz, element.getAsString());
    }

    if (!isPrimitive(clazz)) {
      return PropertyController.getInstance().getGson().fromJson(element, clazz);
    }

    Object parsed = unwrap(element);
    if (parsed == null) {
      return (T) parsed;
    }

    if (parsed instanceof Number) {
      if (parsed.getClass() == clazz) return (T) parsed;

      if (clazz == Integer.class) {
        return (T) ((Object) ((Number) parsed).intValue());
      }

      if (clazz == Float.class) {
        return (T) ((Object) ((Number) parsed).floatValue());
      }

      if (clazz == Double.class) {
        return (T) ((Object) ((Number) parsed).doubleValue());
      }

      if (clazz == Long.class) {
        return (T) ((Object) ((Number) parsed).longValue());
      }
    }

    return (T) parsed;
  }

  @SneakyThrows
  public static <T> T fromSerializable(JsonElement element, Class<T> clazz) {
    T object = (T) DataUtil.getConstructor(clazz).newInstance();
    ((SerializableObject) object).deserialize(new SerializedData(element));
    return object;
  }

  public static boolean isPrimitive(Class clazz) {
    clazz = Primitives.wrap(clazz);
    return primitiveList.contains(clazz);
  }

  @SneakyThrows
  public static Constructor getConstructor(Class clazz) {
    Constructor constructor = clazz.getDeclaredConstructor();
    constructor.setAccessible(true);
    return constructor;
  }
}
