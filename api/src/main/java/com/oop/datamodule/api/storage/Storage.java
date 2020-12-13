package com.oop.datamodule.api.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.api.model.ModelBody;
import com.oop.datamodule.api.model.ModelCachedData;
import com.oop.datamodule.api.storage.lock.ModelLock;
import com.oop.datamodule.api.util.Loadable;
import com.oop.datamodule.api.util.Saveable;
import lombok.AccessLevel;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class Storage<T extends ModelBody> implements Loadable, Saveable, Iterable<T> {
    @Getter(value = AccessLevel.PROTECTED)
    private final Map<String, ModelCachedData> dataCache = new ConcurrentHashMap<>();

    @Getter(value = AccessLevel.PROTECTED)
    private final Map<String, ModelLock<T>> lockMap = new ConcurrentHashMap<>();

    @Getter(value = AccessLevel.PROTECTED)
    private final Set<String> beingRemoved = ConcurrentHashMap.newKeySet();

    @Getter(value = AccessLevel.PROTECTED)
    private final Map<Class<? extends T>, Constructor<? extends T>> constructorMap = new ConcurrentHashMap<>();

    @Getter(AccessLevel.PROTECTED)
    private final List<Consumer<Storage<T>>> onLoad = new LinkedList<>();

    @Getter
    private final Map<String, Class<T>> variants = new HashMap<>();

    public Storage(
            StorageRegistry storageRegistry,
            boolean register
    ) {
        if (register && storageRegistry != null)
            storageRegistry.registerStorage(this);
    }

    public Storage(
            StorageRegistry storageRegistry
    ) {
        this(storageRegistry, true);
    }

    public Storage() {
        this(null, true);
    }

    protected abstract void onAdd(T object);

    protected abstract void onRemove(T object);

    public void add(T object) {
        onAdd(object);
        save(object, true, null);
    }

    public void remove(T object) {
        onRemove(object);
    }

    public void addVariant(String variant, Class<T> clazz) {
        getConstructor(clazz);
        variants.put(variant, clazz);
    }

    public abstract void save(T object, boolean async, Runnable callback);

    public void save(T object, boolean async) {
        save(object, async, null);
    }

    public abstract Stream<T> stream();

    public boolean accepts(Class clazz) {
        return variants.values().stream().anyMatch(c -> clazz.isAssignableFrom(c));
    }

    public void onLoad(Consumer<Storage<T>> onLoad) {
        this.onLoad.add(onLoad);
    }

    public boolean isObjectUpdated(T object) {
        SerializedData data = new SerializedData();
        object.serialize(data);
        return isObjectUpdated(object.getKey(), data);
    }

    public boolean isObjectUpdated(String key, SerializedData data) {
        ModelCachedData modelCachedData = dataCache.computeIfAbsent(key, k -> new ModelCachedData());
        if (modelCachedData.isEmpty()) return true;

        JsonObject jsonObject = data.getJsonElement().getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
            if (modelCachedData.isUpdated(entry.getKey(), entry.getValue().toString()))
                return true;

        return false;
    }

    protected void loadObjectCache(String key, SerializedData data) {
        ModelCachedData modelCachedData = dataCache.computeIfAbsent(key, k -> new ModelCachedData());
        modelCachedData.clear();

        JsonObject jsonObject = data.getJsonElement().getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
            modelCachedData.add(entry.getKey(), entry.getValue().toString());
    }

    protected ModelLock<T> getLock(T object) {
        return lockMap.computeIfAbsent(object.getKey(), k -> new ModelLock<>(object));
    }

    protected void acquireAndLaterRemove(T object, Runnable runnable) {
        if (beingRemoved.contains(object.getKey())) return;

        beingRemoved.add(object.getKey());
        runnable.run();
        beingRemoved.remove(object.getKey());
        lockMap.remove(object.getKey());
        dataCache.remove(object.getKey());
    }

    protected <B extends T> Constructor<B> getConstructor(Class<B> clazz) {
        return (Constructor<B>) constructorMap.computeIfAbsent(clazz, key -> {
            try {
                Constructor<B> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor;
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to find empty constructor for " + clazz.getSimpleName());
            }
        });
    }

    public String findVariantNameFor(Class<?> clazz) {
        for (Map.Entry<String, Class<T>> stringClassEntry : getVariants().entrySet()) {
            if (stringClassEntry.getValue() == clazz)
                return stringClassEntry.getKey();
        }

        throw new IllegalStateException("Failed to find registered variant for " + clazz.getSimpleName());
    }
}
