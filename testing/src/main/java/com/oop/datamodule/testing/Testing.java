package com.oop.datamodule.testing;

import com.google.gson.JsonObject;
import com.oop.datamodule.api.SerializedData;

import java.util.UUID;

public class Testing {
  public static void main(String[] args) {
    JsonObject object = new JsonObject();
    object.addProperty("uuid", "2c5cf4b6-0876-4b0a-8528-43932f8e8337");
    object.addProperty("tokens", "0l");

    SerializedData data = new SerializedData(object);
    System.out.println(UUID.fromString(data.applyAs("uuid", String.class)));
    System.out.println(data.applyAs("tokens", long.class));
  }
}
