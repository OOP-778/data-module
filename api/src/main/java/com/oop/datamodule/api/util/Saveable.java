package com.oop.datamodule.api.util;

public interface Saveable {
  void save(boolean async, Runnable callback);

  default void save() {
    save(true, null);
  }

  default void save(boolean async) {
    save(async, null);
  }

  default void save(Runnable callback) {
    save(true, callback);
  }
}
