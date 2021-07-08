package com.oop.datamodule.property.test;

import com.oop.datamodule.property.impl.PropertyController;

public class Testing {
  public static void main(String[] args) {
    PropertyController propertyController = new PropertyController(4);
    propertyController.execute(() -> {
      System.out.println("wfaf");
    });
  }
}
