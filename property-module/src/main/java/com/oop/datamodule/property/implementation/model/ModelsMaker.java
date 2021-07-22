package com.oop.datamodule.property.implementation.model;

import com.oop.datamodule.property.implementation.storage.ImplGeneratedPropertyStorage;
import com.oop.datamodule.property.implementation.builder.PropertiesBuilder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class ModelsMaker {

  private final ImplGeneratedPropertyStorage makeFor;

  public ModelsMaker newModel(Consumer<ModelBuilder> consumer) {
    final ModelBuilder modelMakerBuilder = new ModelBuilder();
    consumer.accept(modelMakerBuilder);
    return this;
  }

  @Setter
  @Accessors(chain = true, fluent = true)
  public static class ModelBuilder {
    private String modelName;

    public ModelBuilder properties(Consumer<PropertiesBuilder> propertiesBuilderConsumer) {
      return this;
    }
  }
}
