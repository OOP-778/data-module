package com.oop.datamodule.api;

import com.google.gson.JsonElement;
import com.oop.datamodule.api.general.Applyable;
import com.oop.datamodule.api.general.Identifiable;
import com.oop.datamodule.api.general.Saveable;
import com.oop.datamodule.api.holder.DataHolder;
import com.oop.datamodule.api.holder.LoadedValue;

import java.util.Map;

/** A database property wrapper */
public interface Property<T> extends Applyable<Property<T>>, DataHolder<T, LoadedValue<T>>, Identifiable<String> {

  /** Get holder of the property */
  PropertyHolder holder();

  /** Get settings of this property */
  Map<String, Object> getSettings();

  /**
   * Get serialized version of the property The serialized value is cached for 10 seconds in case of
   * repeated calls
   */
  JsonElement serialize();

  /** Save the property */
  default void save(Saveable.SaveArgs saveArgs) {
    holder().save(saveArgs.toBuilder().clearProperties().property(id()).build());
  }
}
