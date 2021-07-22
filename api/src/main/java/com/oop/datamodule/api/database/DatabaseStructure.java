package com.oop.datamodule.api.database;

import java.util.List;
import java.util.Map;

/** This is made by each database type */
public interface DatabaseStructure {

  /** Name of the model in sql name of the table, in mongo name of collection */
  String modelName();

  /** List of all primary keys of the model */
  List<String> primaryKeys();

  /** Map of all properties with their settings Will be sorted by ABC */
  Map<String, Map<String, Object>> properties();
}
