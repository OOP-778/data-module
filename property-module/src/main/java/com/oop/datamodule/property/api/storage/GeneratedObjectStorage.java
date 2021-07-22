package com.oop.datamodule.property.api.storage;

import com.oop.datamodule.property.api.PropertyHolder;
import com.oop.datamodule.property.api.PropertyStorage;

/** Used for generated models */
public interface GeneratedObjectStorage extends PropertyStorage {

  /**
   * Insert new object into the storage It does not save once inserted, you need to call save
   * yourself!
   *
   * @param modelName the name of the model that you have created
   * @param objectArgs is a array of key, value for example insertObject("exampleModel", "uuid",
   *     UUID.randomUUID())) The values must be passed as defined in your model if you defined
   *     properties with default values, there's no need to define them, but if you want override
   *     them you can define here.
   */
  PropertyHolder insertObject(String modelName, Object... objectArgs);
}
