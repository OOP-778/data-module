package com.oop.datamodule.api.util;

public interface Loadable {
  void load(boolean async, Runnable callback);

  default void load() {
    load(true, null);
  }

  default void load(boolean async) {
    load(async, null);
  }

  default void load(Runnable callback) {
    load(true, callback);
  }
}
