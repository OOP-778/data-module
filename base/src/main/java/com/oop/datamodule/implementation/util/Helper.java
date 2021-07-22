package com.oop.datamodule.implementation.util;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class Helper {

  /** Converts array of a key, value objects into a map */
  public Map<String, Object> mapFromArray(Object... objects) {
    if (objects.length == 0) return new HashMap<>();
    if (objects.length % 2 != 0)
      throw new IllegalStateException(
          "Failed to convert objects to map, because the size is not even!");

    Map<String, Object> map = new HashMap<>();

    int len = objects.length;
    int i = 0;

    do {
      Object key = Objects.requireNonNull(objects[i++], "Key cannot be null!");
      Object value = Objects.requireNonNull(objects[i++], "Value cannot be null!");
      map.put(key.toString(), value);
    } while (i != len);

    return map;
  }
}
