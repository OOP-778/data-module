package com.oop.datamodule.property.test;

import com.oop.datamodule.property.api.PropertyHolder;
import com.oop.datamodule.property.implementation.PropertyController;
import com.oop.datamodule.property.implementation.storage.ImplGeneratedPropertyStorage;
import lombok.SneakyThrows;

import java.util.UUID;

public class Testing {
  @SneakyThrows
  public static void main(String[] args) {
        PropertyController propertyController = PropertyController.create();
        ImplGeneratedPropertyStorage storage = new ImplGeneratedPropertyStorage();

        storage
            .modelMaker()
            .newModel(
                modelBuilder -> {
                  modelBuilder.modelName("player_kills");
                  modelBuilder.properties(
                      propsBuilder -> {
                        propsBuilder.newProperty(
                            UUID.class,
                            prop -> {
                              prop.identifier("uuid");
                              prop.markPrimary();

                              prop.addSetting("sql.type", "varchar(16)");
                            });
                      });
                });

        PropertyHolder object = storage.insertObject("player_kills", "uuid", UUID.randomUUID());
  }
}
