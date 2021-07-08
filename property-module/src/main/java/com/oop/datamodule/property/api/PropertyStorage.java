package com.oop.datamodule.property.api;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/** This storage holds all the objects */
public interface PropertyStorage {

  /** Get Internally Registered Data */
  Map<UUID, PropertyHolder> data();

  /** Get values of the map */
  Collection<PropertyHolder> values();

  /** Generate an UUID for an PropertyHolder */
  UUID generateUUID();

}
