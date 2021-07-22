package com.oop.datamodule.api.general;

import java.util.function.Consumer;

public interface Applyable<T> {

    default T apply(Consumer<T> consumer) {
        consumer.accept((T) this);
        return (T) this;
    }
}
