package com.oop.datamodule.property;

import com.oop.datamodule.property.api.Property;
import com.oop.datamodule.property.key.ImplKeyRegistry;

import java.util.concurrent.TimeUnit;

public class Testing {
  public static void main(String[] args) {
    ImplKeyRegistry registry = new ImplKeyRegistry(null);

    Property<Integer> a = null;
    a
            .getBlocking(TimeUnit.SECONDS, 10);
  }
}
