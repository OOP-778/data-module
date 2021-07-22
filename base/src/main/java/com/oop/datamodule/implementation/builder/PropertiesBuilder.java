package com.oop.datamodule.implementation.builder;

import java.util.function.Consumer;

public class PropertiesBuilder {
  public <T> PropertiesBuilder newProperty(
      Class<T> type, Consumer<PropertyBuilder<T>> builderConsumer) {
    final PropertyBuilder<T> builder = new PropertyBuilder<>(type);
    builderConsumer.accept(builder);
    return this;
  }
}
