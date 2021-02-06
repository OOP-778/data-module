package com.oop.datamodule.testing;

import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import java.util.concurrent.ThreadLocalRandom;

public class SubObject implements SerializableObject {

  private int value = ThreadLocalRandom.current().nextInt(200000);
  private int secondValue = ThreadLocalRandom.current().nextInt(200000);

  @Override
  public void serialize(SerializedData data) {
    data.write("value", value);
    data.write("second", secondValue);
  }

  @Override
  public void deserialize(SerializedData data) {
    this.value = data.applyAs("value", int.class);
    this.secondValue = data.applyAs("second", int.class);
  }
}
