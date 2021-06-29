package com.oop.datamodule.property.api.key;

import java.util.Optional;

public interface KeyRegistry {

  Optional<KeyRegistry> parent();

  Optional<String> key();

  boolean isMutable();

  /** Get PropertyKey from a string, will throw errors if key is incorrect */
  PropertyKey getKey(String value);

  /**
   * Get optional property key
   */
  Optional<PropertyKey> getOptionalKey(String key);

  /**
   * Get key registry from an key of the registry
   */
  Optional<KeyRegistry> getRegistry(String key);
}
