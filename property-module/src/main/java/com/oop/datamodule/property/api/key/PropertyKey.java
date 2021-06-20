package com.oop.datamodule.property.api.key;

import com.oop.datamodule.property.api.general.Applyable;

import java.util.Optional;

public interface PropertyKey extends Applyable<PropertyKey> {
  /** Which registry this key is part of */
  KeyRegistry registry();

  /** Does it contain a parent */
  Optional<String> parent();

  /** Identifier of the key */
  String identifier();
}
