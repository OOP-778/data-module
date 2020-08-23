package com.oop.datamodule.body;

public interface FlatDataBody extends DataBody {
    /**
     * Primary key of the object
     *
     * @return serialized primary key
     */
    String getKey();

    /**
     * Get the type of the data to add support for multiple types
     * @return a type of the object
     */
    String getSerializedType();
}
