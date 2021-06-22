package com.oop.datamodule.property.api.holder;

public interface LoadedValue<T> {
    boolean isUpdated();

    void set(T newValue);

    T get();
}
