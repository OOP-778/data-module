package com.oop.datamodule.property.api.context;

import com.oop.datamodule.property.api.Property;
import com.oop.datamodule.property.api.general.Applyable;
import com.oop.datamodule.property.api.general.PropertyHolder;
import com.oop.datamodule.property.api.key.PropertyKey;

import java.util.Optional;

/** This holds data about every possible context that user has defined */
public interface Contexts<T> extends Applyable<Contexts<T>>, PropertyHolder<Property<T>> {

  /** Get specific context properties from an key */
  Optional<ContextProperties> propertiesOf(PropertyKey key);
}
