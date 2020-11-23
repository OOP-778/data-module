package com.oop.datamodule.api.model;

import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.util.Saveable;

public interface ModelBody extends SerializableObject, Saveable {
    /**
     * Primary key of the object
     *
     * @return serialized primary key
     */
    String getKey();
}
