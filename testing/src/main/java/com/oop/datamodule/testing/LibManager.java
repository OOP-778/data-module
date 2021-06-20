package com.oop.datamodule.testing;

import com.oop.datamodule.api.loader.LibraryManager;
import com.oop.datamodule.api.loader.logging.adapters.LogAdapter;

import java.nio.file.Path;

public class LibManager extends LibraryManager {

  /**
   * Creates a new library manager.
   *
   * @param logAdapter plugin logging adapter
   * @param dataDirectory plugin's data directory
   */
  protected LibManager(LogAdapter logAdapter, Path dataDirectory) {
    super(logAdapter, dataDirectory);
  }
}
