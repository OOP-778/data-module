package com.oop.datamodule.property.api.context;

import java.util.Optional;

/** A sort of Map which holds values of specific Context */
public interface ContextProperties {

  /** Get specific property from properties */
  <T> Optional<T> get(String key, Class<T> type);
}
