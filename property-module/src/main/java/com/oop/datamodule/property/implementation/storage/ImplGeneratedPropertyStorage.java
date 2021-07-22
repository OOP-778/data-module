package com.oop.datamodule.property.implementation.storage;

import com.oop.datamodule.api.util.Preconditions;
import com.oop.datamodule.property.api.Property;
import com.oop.datamodule.property.api.PropertyHolder;
import com.oop.datamodule.property.implementation.ImplPropertyHolder;
import com.oop.datamodule.property.implementation.ImplPropertyStorage;
import com.oop.datamodule.property.implementation.model.ModelsMaker;
import com.oop.datamodule.property.implementation.util.Helper;
import lombok.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class ImplGeneratedPropertyStorage extends ImplPropertyStorage {

  // Generated model generators
  private final Map<String, Function<Map<String, Object>, GeneratedModel>> generators =
      new TreeMap<>(String::compareToIgnoreCase);

  public ModelsMaker modelMaker() {
    return new ModelsMaker(this);
  }

  public PropertyHolder insertObject(@NonNull String modelName, Object... args) {
    final Function<Map<String, Object>, GeneratedModel> objectGenerator = generators.get(modelName);
    Preconditions.checkArgument(
        objectGenerator != null, String.format("Model by name: %s doesn't exist!", modelName));

    final Map<String, Object> generatorProps = Helper.mapFromArray(args);
    return objectGenerator.apply(generatorProps);
  }

  @Override
  public void save(SaveArgs saveArgs) {}

  public static class GeneratedModel extends ImplPropertyHolder {
    public GeneratedModel(
        ImplGeneratedPropertyStorage storage,
        LinkedHashMap<String, Property<?>> properties,
        String modelId) {
      super(storage, properties, modelId);
    }
  }
}
