package com.oop.datamodule.body;

import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.util.Saveable;

public interface DataBody extends SerializableObject, Saveable {
    /**
     * Remove the data from database
     */
    void remove();
}
