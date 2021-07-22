package com.oop.datamodule.implementation;

import com.oop.datamodule.api.Property;
import com.oop.datamodule.api.PropertyHolder;
import com.oop.datamodule.api.PropertyStorage;
import com.oop.datamodule.api.general.Saveable;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class ImplPropertyHolder implements PropertyHolder {
  private final ImplPropertyStorage storage;
  private final LinkedHashMap<String, Property<?>> properties;
  private final String modelId;

  @Override
  public PropertyStorage storage() {
    return storage;
  }

  @Override
  public Map<String, Property<?>> properties() {
    return Collections.unmodifiableMap(properties);
  }

  @Override
  public String modelId() {
    return modelId;
  }

  @Override
  public void save(Saveable.SaveArgs data) {
    storage.modelsStructures.get(modelId);
  }

  @Override
  public <T> Property<T> property(String identifier, Class<T> type) {
    return (Property<T>)
        Objects.requireNonNull(
            properties.get(identifier),
            String.format("Invalid property by identifier %s", identifier));
  }
}
