package com.oop.datamodule.storage;

import com.google.common.base.Preconditions;
import com.oop.datamodule.StorageHolder;
import com.oop.datamodule.body.DataBody;
import com.oop.datamodule.util.Loadable;
import com.oop.datamodule.util.Saveable;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public abstract class Storage<T extends DataBody> implements Loadable, Saveable, Iterable<T> {

    @Getter
    private final StorageHolder storageHolder;

    private Map<Class<? extends T>, Constructor<? extends T>> constructorMap = new ConcurrentHashMap<>();

    public Storage(StorageHolder storageHolder) {
        this.storageHolder = storageHolder;
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

    public abstract void save(T object, boolean async, Runnable callback);

    public abstract Stream<T> stream();

    public abstract boolean accepts(Class clazz);

    @SneakyThrows
    protected  <B extends T> Constructor<B> getConstructor(Class<B> clazz) {
        return (Constructor<B>) constructorMap.computeIfAbsent(clazz, key -> {
            Constructor<B> constructor = clazz.getDeclaredConstructor();
            Preconditions.checkArgument(constructor != null, "Failed to find constructor for " + clazz.getSimpleName());
            constructor.setAccessible(true);
            return constructor;
        });
    }
}
