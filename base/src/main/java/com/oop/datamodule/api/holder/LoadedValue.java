package com.oop.datamodule.api.holder;

public interface LoadedValue<T> {
    boolean isUpdated();

    void set(T newValue);

    T get();
}
