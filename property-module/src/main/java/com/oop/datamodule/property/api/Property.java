package com.oop.datamodule.property.api;

import com.oop.datamodule.property.api.context.Contexts;
import com.oop.datamodule.property.api.holder.DataHolder;
import com.oop.datamodule.property.api.holder.LoadedValue;
import com.oop.datamodule.property.api.queue.TaskQueue;
import com.oop.datamodule.property.api.general.Applyable;

/** A database property wrapper */
public interface Property<T> extends Applyable<Property<T>>, DataHolder<T, LoadedValue<T>> {

  /** This identifier acts as a name of column */
  String identifier();

  /*
  Get properties of different contexts
  */
  Contexts<T> contexts();

  /*
  Get queue of tasks waiting of this property
  */
  TaskQueue<T> taskQueue();

}
