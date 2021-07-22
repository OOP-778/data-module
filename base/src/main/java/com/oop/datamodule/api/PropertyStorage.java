package com.oop.datamodule.api;

import com.oop.datamodule.api.database.DatabaseHandler;
import com.oop.datamodule.api.general.Identifiable;
import com.oop.datamodule.api.general.Saveable;
import com.oop.datamodule.store.Store;
import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/** This storage holds all the objects */
public interface PropertyStorage extends Saveable, Identifiable<String> {

  /**
   * Get unmodifiable registered data the key is the one type of what you defined when creating the
   * primary property {@link Store} allows us to fetch a specific holder from the store by using
   * multiple primary keys that are set upon creating a model
   */
  Optional<Store<PropertyHolder>> dataOf(String modelName);

  /** Get all model names available for this storage */
  Set<String> models();

  /** Get values of the map of specific model */
  Optional<Collection<PropertyHolder>> valuesOf(@NonNull String modelName);

  /** Get current database handler */
  Optional<DatabaseHandler> databaseHandler();

  /** Set current database handler */
  void databaseHandler(
      DatabaseHandler databaseHandler);

  /** Remove the holder from the storage */
  void remove(PropertyHolder holder);

  /**
   * Remove one or multiple objects
   * @param modelName the name of the model
   * @param args key, value, key, value ~ the key is the name of property and value is the value of it
   */
  void removeObject(String modelName, Object... args);
}
