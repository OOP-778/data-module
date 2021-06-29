package com.oop.datamodule.property.impl;

import com.oop.datamodule.property.api.Property;
import com.oop.datamodule.property.api.PropertyHolder;
import com.oop.datamodule.property.api.key.KeyRegistry;
import com.oop.datamodule.property.api.key.PropertyKey;
import com.oop.datamodule.property.impl.builder.PropertyBuilder;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.LinkedList;

@UtilityClass
public class PropertyHelper {

  private static final String DB_KEY = "databases";

  private static final PropertyController propertyController = new PropertyController();

  public PropertyController propertyController() {
    return propertyController;
  }

  public PropertyKey key(String key) {
    return propertyController().getBaseKeyRegistry().getKey(key);
  }

  public KeyRegistry databaseKeys() {
    KeyRegistry registry =
        propertyController().getBaseKeyRegistry().getRegistry(DB_KEY).orElse(null);
    if (registry == null) {
      propertyController().getBaseKeyRegistry().registerParents(DB_KEY);
    }

    return databaseKeys();
  }

  public PropertyKey databaseKey(String key) {
    return databaseKeys().getKey(key);
  }

  public <T> PropertyBuilder<T> propertyBuilder(Class<T> clazz) {
    return new PropertyBuilder<>(clazz);
  }

  public PropertyHolder makePropertyHolderOf(Property<?>... properties) {
    return ImplPropertyHolder.of(properties);
  }

  @SneakyThrows
  public PropertyHolder makePropertyHolderWithReflection(Object object) {
    final LinkedList<Property<?>> properties = new LinkedList<>();
    for (Field field : object.getClass().getFields()) {
      if (!field.getType().isAssignableFrom(Property.class)) continue;

      properties.add((Property<?>) field.get(object));
    }

    return makePropertyHolderOf(properties.toArray(new Property[0]));
  }
}
