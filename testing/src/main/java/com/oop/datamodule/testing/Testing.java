package com.oop.datamodule.testing;

import com.google.gson.JsonObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageInitializer;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.loader.logging.adapters.JDKLogAdapter;
import com.oop.datamodule.h2.H2Credential;
import com.oop.datamodule.h2.H2Dependencies;
import com.oop.datamodule.universal.StorageProviders;
import com.oop.datamodule.universal.provider.StorageProvider;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Logger;

public class Testing {
  public static void main(String[] args) {
    Path path = Paths.get(System.getProperty("user.dir"));
    LibManager test = new LibManager(new JDKLogAdapter(Logger.getLogger("TEST")), path);
    StorageInitializer.initialize(
            Runnable::run,
            Runnable::run,
            test,
            null,
            Throwable::printStackTrace,
            (URLClassLoader) Testing.class.getClassLoader(),
            new H2Dependencies()
    );


    StorageRegistry registry = new StorageRegistry();
    TEstSingleStorage universalBodyModels = new TEstSingleStorage(registry);
    universalBodyModels.currentImplementation(StorageProviders.H2.provide(universalBodyModels.getLinker(), new H2Credential().folder(path.toFile()).database("test").build()));

    universalBodyModels.save();

  }
}
