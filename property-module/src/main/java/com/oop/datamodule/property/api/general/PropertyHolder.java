package com.oop.datamodule.property.api.general;

import com.oop.datamodule.property.api.Property;
import lombok.NonNull;

public interface PropertyHolder<T> {
    @NonNull
    Property<T> property();
}
