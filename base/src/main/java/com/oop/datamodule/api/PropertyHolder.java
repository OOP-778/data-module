package com.oop.datamodule.api;

import com.oop.datamodule.api.general.Saveable;
import lombok.NonNull;

import java.util.Map;

/** A class which holds the properties */
public interface PropertyHolder {

  /** Get storage of this holder */
  PropertyStorage storage();

  /** Get all properties */
  Map<String, Property<?>> properties();

  /** Save specific properties */
  default void saveAll(@NonNull Saveable.SaveArgs saveArgs) {
    save(saveArgs.toBuilder().clearProperties().properties(properties().keySet()).build());
  }

  /**
   * Get model id
   */
  String modelId();

  /**
   * Save specific properties
   *
   * @param data SaveArgs build
   */
  void save(@NonNull Saveable.SaveArgs data);

  /** Get a property */
  <T> Property<T> property(String identifier, Class<T> type);
}
