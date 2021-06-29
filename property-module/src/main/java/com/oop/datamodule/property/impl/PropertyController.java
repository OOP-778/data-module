package com.oop.datamodule.property.impl;

import com.oop.datamodule.property.impl.key.ImplKeyRegistry;
import lombok.Getter;

@Getter
public class PropertyController {
  private final ImplKeyRegistry baseKeyRegistry = new ImplKeyRegistry(null);
}
