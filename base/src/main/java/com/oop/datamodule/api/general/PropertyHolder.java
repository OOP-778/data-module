package com.oop.datamodule.api.general;

import com.oop.datamodule.api.Property;
import lombok.NonNull;

public interface PropertyHolder<T> {
    @NonNull
    Property<T> property();
}
