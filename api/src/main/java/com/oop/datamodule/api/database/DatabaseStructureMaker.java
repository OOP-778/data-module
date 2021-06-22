package com.oop.datamodule.api.database;

@FunctionalInterface
public interface DatabaseStructureMaker<O extends DatabaseStructure> {

  /**
   * Make structure based of databaseHandler
   */
  O make(DatabaseHandler<?, ?> databaseHandler);

}
