package com.oop.datamodule.property.api.storage;

import com.oop.datamodule.property.api.PropertyStorage;
import com.oop.datamodule.property.api.runner.RunnerContext;

/**
 * Used for defined objects that holds properties instead of generated
 *
 * @param <TYPE> the type of your object
 */
public interface ObjectBasedStorage<TYPE> extends PropertyStorage {

  /**
   * Insert new object into the storage It does not save once inserted, you need to call save
   * yourself!
   *
   * @param object the TYPED object that holds properties
   */
  void insertObject(TYPE object);

  /**
   * Remove object from the storage Uses default {@link RunnerContext#asyncRunner()} to remove from
   * the database If the object is somehow not stored, it will be ignored
   *
   * @param object the TYPED object that holds properties
   */
  void removeObject(TYPE object);
}
