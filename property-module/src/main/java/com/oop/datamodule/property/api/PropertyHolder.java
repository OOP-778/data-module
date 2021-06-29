package com.oop.datamodule.property.api;

import java.util.LinkedHashMap;
import java.util.UUID;

/** A class which holds the properties */
public interface PropertyHolder<T> {

  /** Get object that wraps this */
  T getWrappingObject();

  /** Get storage of this holder */
  PropertyStorage<T> storage();

  /** Get internal UUID of the object */
  UUID holderUUID();

  /** Get all properties */
  LinkedHashMap<String, Property<?>> properties();

  /** Save all fields */
  void saveAll();
}
