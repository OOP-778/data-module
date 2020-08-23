package com.oop.datamodule;

public interface SerializableObject {
    void serialize(SerializedData data);

    void deserialize(SerializedData data);
}
