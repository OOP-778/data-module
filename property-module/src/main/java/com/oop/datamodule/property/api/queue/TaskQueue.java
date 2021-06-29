package com.oop.datamodule.property.api.queue;

import com.oop.datamodule.property.api.general.PropertyHolder;
import com.oop.datamodule.property.api.key.PropertyKey;
import com.oop.datamodule.property.api.general.Applyable;

import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

/**
 * This queue holds all the tasks of an property
 * This is used when an value is not loaded and it calls :)
 */
public interface TaskQueue<T> extends Applyable<TaskQueue<T>>, PropertyHolder<T> {

  /** An transaction is when value is either loaded, set or disposed */
  Map<PropertyKey, Function<T, T>> ensurers();

  /** This is queue of tasks waiting when the value will be loaded */
  Queue<Function<T, T>> queue();
}
