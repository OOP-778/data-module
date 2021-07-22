package com.oop.datamodule.property.implementation.builder;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.util.Preconditions;
import com.oop.datamodule.property.api.Property;
import com.oop.datamodule.property.implementation.CommonPropertyKeys;
import com.oop.datamodule.property.implementation.PropertyController;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public class PropertyBuilder<T> {

  /*
  Type of the property
   */
  private final @NonNull Class<T> type;

  // Settings of the property
  private final Map<String, Object> settings = new HashMap<>();

  @Setter private String identifier;

  public PropertyBuilder(Class<T> clazz) {
    this.type = clazz;
  }

  public Property<T> build() {
    Preconditions.checkArgument(
        this.identifier != null, "Identifier of property must not be null!");

    // Find serializer for the type
    final Gson gson = PropertyController.getInstance().getGson();
    TypeAdapter<T> adapter = gson.getAdapter(type);
    if (adapter == null && !SerializableObject.class.isAssignableFrom(type)) {
      throw new IllegalStateException(
          String.format(
              "Failed to build property with identifier %s cause no valid serializer found for %s type",
              identifier, type));
    }

    return null;
  }

  /**
   * Mark the property as primary key Used for searching indexes, primary keys should never be
   * unloaded!
   */
  public PropertyBuilder<T> markPrimary() {
    return addSetting(CommonPropertyKeys.PRIMARY_KEY, true).markFinal();
  }

  public PropertyBuilder<T> markFinal() {
    return addSetting(CommonPropertyKeys.FINAL, true);
  }

  public PropertyBuilder<T> defaultValue(Supplier<T> supplier) {
    return this;
  }

  public PropertyBuilder<T> addSetting(String key, Object value) {
    settings.put(key, value);
    return this;
  }

  public PropertyBuilder<T> ensurer(Function<T, T> ensurer) {
    return this;
  }
}
