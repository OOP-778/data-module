package com.oop.datamodule.property.api.holder;

import com.oop.datamodule.property.api.general.PropertyHolder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * This is where the data from db is stored
 */
public interface DataHolder<T> extends PropertyHolder<T> {

    /**
     * Get and load if not present as blocking
     */
    T getBlocking(TimeUnit unit, long timeout);

    /**
     * Get from db async if loaded will return instantly
     */
    CompletableFuture<T> getAsync();

}
