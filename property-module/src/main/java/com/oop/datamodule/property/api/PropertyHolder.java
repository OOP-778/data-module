package com.oop.datamodule.property.api;

import java.util.LinkedHashMap;
import java.util.UUID;

/** A class which holds the properties */
public interface PropertyHolder {

  /** Get storage of this holder */
  PropertyStorage storage();

  /** Set storage of this holder */
  void storage(PropertyStorage storage);

  /** Get internal UUID of the object */
  UUID holderUUID();

  /** Get all properties */
  LinkedHashMap<String, Property<?>> properties();

  /** Save all loaded fields */
  void saveAll();
}
