package com.oop.datamodule.property.impl.builder;

import com.oop.datamodule.property.api.key.PropertyKey;
import lombok.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ContextsBuilder<T> {
  private final Map<PropertyKey, PropertiesBuilder<T>> contexts = new LinkedHashMap<>();

  public ContextsBuilder<T> addContext(
      @NonNull PropertyKey key, Consumer<PropertiesBuilder<T>> propertiesBuilderConsumer) {
    final PropertiesBuilder<T> propertiesBuilder = new PropertiesBuilder<>();
    propertiesBuilderConsumer.accept(propertiesBuilder);

    this.contexts.put(key, propertiesBuilder);
    return this;
  }

  public ContextsBuilder<T> removeContext(@NonNull PropertyKey key) {
    this.contexts.remove(key);
    return this;
  }
}
