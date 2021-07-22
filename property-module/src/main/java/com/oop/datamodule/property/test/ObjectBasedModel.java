package com.oop.datamodule.property.test;

import com.oop.datamodule.property.api.Property;
import com.oop.datamodule.property.implementation.builder.PropertyBuilder;

import java.util.UUID;

public class ObjectBasedModel {
  private final Property<UUID> userUuid =
      new PropertyBuilder<>(UUID.class).markPrimary().identifier("uuid").build();

  private final Property<Double> balance =
      new PropertyBuilder<>(Double.class)
          .identifier("balance")
          // Make sure the balance newer gets below 0
          .ensurer(modifiedBalance -> modifiedBalance < 0 ? 0 : modifiedBalance)
          .defaultValue(() -> 0.0)
          .build();
}
