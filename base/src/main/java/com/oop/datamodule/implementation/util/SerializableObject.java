package com.oop.datamodule.implementation.util;

import com.oop.datamodule.api.SerializedData;

public interface SerializableObject {
  void serialize(SerializedData data);

  void deserialize(SerializedData data);
}
