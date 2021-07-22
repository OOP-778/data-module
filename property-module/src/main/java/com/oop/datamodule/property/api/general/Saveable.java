package com.oop.datamodule.property.api.general;

import com.oop.datamodule.property.api.runner.RunnerContext;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.function.Consumer;

public interface Saveable {
  /** @param saveArgs built {@link SaveArgs} */
  void save(SaveArgs saveArgs);

  /** Save using default runner context */
  default void save() {
    save(SaveArgs.builder().build());
  }

  @Builder(toBuilder = true)
  @Getter
  class SaveArgs {
    /** Which properties this Save affects */
    @Singular("property")
    private final List<String> properties;

    /**
     * Should it force the save, usually this should always be at false so it doesn't try
     * serializing/updating properties that are already there. But in case when you have switched to
     * new {@link com.oop.datamodule.api.database.DatabaseHandler} you should set force to true
     */
    @Builder.Default private final boolean force = false;

    /** Which runner context are we using? */
    @NonNull @Builder.Default
    private final RunnerContext runnerContext = RunnerContext.asyncRunner();

    /** Callback whenever the save process has finished */
    @Builder.Default private final Consumer<String> callback = null;
  }
}
