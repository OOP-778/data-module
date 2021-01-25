package com.oop.datamodule.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.oop.datamodule.api.loader.Library;
import com.oop.datamodule.api.loader.LibraryManager;
import com.oop.datamodule.api.loader.StorageDependencies;
import com.oop.datamodule.api.util.DataPair;
import com.oop.datamodule.api.util.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StorageInitializer {
    private static StorageInitializer instance;

    public void onDisable() {
        instance = null;
    }

    public static StorageInitializer getInstance() {
        Objects.requireNonNull(instance, "StorageInitializer hasn't been setup!");
        return instance;
    }

    /**
     * Initialize StorageInitializer
     * @param asyncRunner consumer which runs async tasks
     * @param syncRunner consumer which runs sync tasks
     * @param onBuild can be nullable, you can consume GsonBuilder to add own params
     * @param errorHandler whenever error is thrown, it will go thru this
     * @param dependencies dependencies that storages uses
     * @return a pair of StorageInitializer instance and hook to run when application shut downs to clean instance
     */
    public static DataPair<StorageInitializer, Runnable> initialize(
            @NonNull Consumer<Runnable> asyncRunner,
            @NonNull Consumer<Runnable> syncRunner,
            @NonNull LibraryManager libraryManager,
            Consumer<GsonBuilder> onBuild,
            Consumer<Throwable> errorHandler,
            @NonNull StorageDependencies ...dependencies
    ) {
        Preconditions.checkArgument(instance == null, "Instance of StorageInitializer already exists!");
        instance = new StorageInitializer();
        instance.asyncRunner = asyncRunner;
        instance.syncRunner = syncRunner;
        instance.onBuild = onBuild;
        instance.errorHandler = errorHandler;
        instance.loadLibraries(libraryManager, dependencies);

        return new DataPair<>(instance, () -> instance = null);
    }

    private void loadLibraries(LibraryManager libraryManager, StorageDependencies ...dependencies) {
        libraryManager.addJCenter();

        Set<Library> libraries = Arrays
                .stream(dependencies)
                .flatMap(d -> d.getLibraries().stream())
                .collect(Collectors.toCollection(HashSet::new));

        Set<String> repos = Arrays
                .stream(dependencies)
                .flatMap(d -> d.getRepositories().stream())
                .collect(Collectors.toCollection(HashSet::new));

        for (String repo : repos)
            libraryManager.addRepository(repo);

        for (Library library : libraries)
            libraryManager.loadLibrary(library);
    }

    private Consumer<Runnable> asyncRunner;
    private Consumer<Runnable> syncRunner = Runnable::run;
    private Consumer<GsonBuilder> onBuild;

    @Getter
    private Consumer<Throwable> errorHandler;

    private final Map<Class<?>, AdapterObject<?>> adapters = new HashMap<>();
    private Gson gson;

    private StorageInitializer() {}

    @Getter
    @AllArgsConstructor
    public static class AdapterObject<T> {
        private final boolean hierarchyEnabled;
        private final Class<T> baseClass;
        private final TypeAdapter<T> adapter;
    }

    /**
     * Register new adapter for specific object
     * @param clazz the class of the object
     * @param hierarchyEnabled if hierarchy is enabled then it will also work on objects that implements or extends base class
     * @param adapter adapter which serializes / deserializes objects
     */
    public <T extends Object> void registerAdapter(Class<T> clazz, boolean hierarchyEnabled, TypeAdapter<T> adapter) {
        adapters.put(clazz, new AdapterObject<>(hierarchyEnabled, clazz, adapter));
    }

    public <T> Optional<AdapterObject<T>> findAdapter(Class<T> clazz) {
        for (Map.Entry<Class<?>, AdapterObject<?>> entry : adapters.entrySet()) {
            if ((entry.getValue().isHierarchyEnabled() && entry.getKey().isAssignableFrom(clazz)) || entry.getKey() == clazz)
                return Optional.of((AdapterObject<T>) entry.getValue());
        }

        return Optional.empty();
    }

    public Gson getGson() {
        if (gson == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.serializeNulls();
            builder.disableHtmlEscaping();
            if (onBuild != null)
                onBuild.accept(builder);
            gson = builder.create();
        }

        return gson;
    }

    public Gson getPrettyfiedGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        builder.setPrettyPrinting();
        builder.disableHtmlEscaping();
        if (onBuild != null)
            onBuild.accept(builder);

        return builder.create();
    }

    public Consumer<Runnable> getRunner(boolean async) {
        Consumer<Runnable> runner = async ? asyncRunner : syncRunner;
        return runner;
    }
}
