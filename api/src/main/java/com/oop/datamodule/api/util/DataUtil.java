package com.oop.datamodule.api.util;

import com.google.gson.*;
import com.google.gson.internal.Primitives;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
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

  public static JsonElement primitiveToString(Object object) {
    if (object == null) return JsonNull.INSTANCE;

    Class clazz = Primitives.wrap(object.getClass());
    if (clazz == Integer.class) return new JsonPrimitive(object.toString() + "i");
    else if (clazz == Double.class) return new JsonPrimitive(object.toString() + "d");
    else if (clazz == Long.class) return new JsonPrimitive(object.toString() + "l");
    else if (clazz == Float.class) return new JsonPrimitive(object.toString() + "f");
    else if (clazz == Boolean.class)
      return new JsonPrimitive((((Boolean) object) ? "1" : "0") + "b");
    else if (clazz == String.class) return new JsonPrimitive(object.toString());
    else
      throw new IllegalStateException(
          "Failed to serialize object of type " + object.getClass().getSimpleName());
  }

  public static Object fromElement(JsonElement element) {
    try {
      String elementString = element.getAsString();
      elementString = elementString.replaceAll("\"", "");
      char endsWith = elementString.charAt(elementString.length() - 1);
      String content = elementString.substring(0, elementString.length() - 1);

      if (endsWith == 'i') return Integer.valueOf(content);
      else if (endsWith == 'd') return Double.valueOf(content);
      else if (endsWith == 'f') return Float.valueOf(content);
      else if (endsWith == 'l') return Long.valueOf(content);
      else if (endsWith == 'b' && content.length() == 1) return content.toCharArray()[0] != '0';

    } catch (Throwable throwable) {
      return element.getAsString();
    }
    return element.getAsString();
  }

  public static JsonElement wrap(Object object) {
    if (object == null) {
      return JsonNull.INSTANCE;
    }

    // If primitive just convert to string
    if (DataUtil.isPrimitive(object.getClass())) {
      return DataUtil.primitiveToString(object);
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
    if (SerializableObject.class.isAssignableFrom(clazz))
      return fromSerializable(element.getAsJsonObject(), clazz);

    if (clazz.isEnum()) return (T) Enum.valueOf((Class<Enum>) clazz, element.getAsString());

    if (!isPrimitive(clazz)) {
      Optional<? extends StorageInitializer.AdapterObject> adapter =
          StorageInitializer.getInstance().findAdapter(clazz);
      if (adapter.isPresent()) return (T) adapter.get().getAdapter().fromJsonTree(element);
      else return StorageInitializer.getInstance().getGson().fromJson(element, clazz);
    }

    Object parsed = fromElement(element);
    if (clazz != Long.class && parsed.getClass() == Long.class)
      throw new IllegalStateException(
          "Incompatible conversion between "
              + parsed.getClass().getSimpleName()
              + " and "
              + clazz.getSimpleName());

    if (clazz != parsed.getClass()) {
      if (clazz == Boolean.class)
        throw new IllegalStateException(
            "Incompatible conversion from " + parsed.getClass().getSimpleName() + " to Boolean!");

      parsed = doConversion(parsed, clazz);
    }

    return (T) parsed;
  }

  @SneakyThrows
  public static <T> T fromSerializable(JsonObject jsonObject, Class<T> clazz) {
    T object = (T) DataUtil.getConstructor(clazz).newInstance();
    ((SerializableObject) object).deserialize(new SerializedData(jsonObject));
    return object;
  }

  private static <T> Object doConversion(Object parsed, Class<T> clazz) {
    String value = parsed.toString();
    if (clazz == String.class) return value;

    value = value.replaceAll("\"", "");

    clazz = Primitives.wrap(clazz);
    if ((parsed.getClass() == Double.class || parsed.getClass() == Float.class)
        && Primitives.wrap(clazz) == Integer.class) {
      if (value.contains(".")) {
        String[] split = value.split("\\.");
        int noZeroCounter = 0;
        for (char c : split[1].toCharArray()) if (c != '0') noZeroCounter++;

        if (noZeroCounter == 0) value = split[0];
      }
    }

    if (clazz == Integer.class) return Integer.valueOf(value);
    else if (clazz == Long.class) return Long.valueOf(value);
    else if (clazz == Float.class) return Float.valueOf(value);
    else if (clazz == Double.class) return Double.valueOf(value);
    else
      throw new IllegalStateException(
          "Failed to convert "
              + parsed.getClass().getSimpleName()
              + " to "
              + clazz.getSimpleName()
              + " unknown conversion!");
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
