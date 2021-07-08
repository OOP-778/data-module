package com.oop.datamodule.property.impl;

import com.oop.datamodule.api.database.DatabaseHandler;
import com.oop.datamodule.api.database.DatabaseStructure;
import com.oop.datamodule.property.api.Property;
import com.oop.datamodule.property.api.PropertyHolder;
import com.oop.datamodule.property.api.PropertyStorage;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
public class ImplPropertyHolder implements PropertyHolder {
  private PropertyStorage storage;
  private UUID uuid;
  private final LinkedHashMap<String, Property<?>> properties;

  @Setter
  private DatabaseHandler<? extends DatabaseStructure, ? extends DatabaseHandler<?, ?>> handler;

  protected ImplPropertyHolder() {
    this(new LinkedHashMap<>());
  }

  public static ImplPropertyHolder of(Property<?>... properties) {
    return new ImplPropertyHolder(
        new LinkedHashMap<>(
            Arrays.stream(properties)
                .collect(Collectors.toMap(Property::identifier, property -> property))));
  }

  @Override
  public void storage(PropertyStorage storage) {
    this.uuid = storage.generateUUID();
    this.storage = storage;
  }

  @Override
  public UUID holderUUID() {
    return uuid;
  }

  @Override
  public void saveAll() {}
}
