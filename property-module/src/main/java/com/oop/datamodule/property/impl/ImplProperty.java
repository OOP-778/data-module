package com.oop.datamodule.property.impl;

import com.oop.datamodule.property.api.Property;
import com.oop.datamodule.property.api.context.Contexts;
import com.oop.datamodule.property.api.holder.LoadedValue;
import com.oop.datamodule.property.api.queue.TaskQueue;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ImplProperty<T> implements Property<T> {

    @Override
    public String identifier() {
        return null;
    }

    @Override
    public Contexts<T> contexts() {
        return null;
    }

    @Override
    public TaskQueue<T> taskQueue() {
        return null;
    }

    @Override
    public @NonNull Property property() {
        return null;
    }

    @Override
    public LoadedValue<T> getBlocking(TimeUnit unit, long timeout) {
        return null;
    }

    @Override
    public CompletableFuture<LoadedValue<T>> getAsync() {
        return null;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }
}
