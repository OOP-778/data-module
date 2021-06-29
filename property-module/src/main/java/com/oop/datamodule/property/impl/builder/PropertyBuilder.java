package com.oop.datamodule.property.impl.builder;

import com.oop.datamodule.api.util.Preconditions;
import com.oop.datamodule.property.api.Property;
import com.oop.datamodule.property.impl.ImplProperty;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public class PropertyBuilder<T> {

  /*
  Type of the property
   */
  private final @NonNull Class<T> type;

  @Setter private String identifier;

  private ContextsBuilder<T> contextsBuilder = new ContextsBuilder<>();

  public PropertyBuilder(Class<T> clazz) {
    this.type = clazz;
  }

  public PropertyBuilder<T> buildContexts(Consumer<ContextsBuilder<T>> contextsBuilderConsumer) {
    contextsBuilderConsumer.accept(contextsBuilder);
    return this;
  }

  public Property<T> makeProperty() {
    Preconditions.checkArgument(
        this.identifier != null, "Identifier of property must not be null!");
    return new ImplProperty<>();
  }
}
