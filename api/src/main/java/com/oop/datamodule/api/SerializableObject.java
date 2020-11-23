package com.oop.datamodule.api;

public interface SerializableObject {
    void serialize(SerializedData data);

    void deserialize(SerializedData data);
}
