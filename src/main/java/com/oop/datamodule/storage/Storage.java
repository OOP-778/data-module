package com.oop.datamodule.storage;

import com.oop.datamodule.StorageHolder;
import com.oop.datamodule.body.DataBody;
import com.oop.datamodule.util.Loadable;
import com.oop.datamodule.util.Saveable;
import lombok.AccessLevel;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class Storage<T extends DataBody> implements Loadable, Saveable, Iterable<T> {

    @Getter
    private final StorageHolder storageHolder;

    @Getter(AccessLevel.PROTECTED)
    private List<Consumer<Storage<T>>> onLoad = new LinkedList<>();

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

    protected  <B extends T> Constructor<B> getConstructor(Class<B> clazz) {
        return (Constructor<B>) constructorMap.computeIfAbsent(clazz, key -> {
            try {
                Constructor<B> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor;
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to find constructor for " + clazz.getSimpleName());
            }
        });
    }

    public void onLoad(Consumer<Storage<T>> onLoad) {
        this.onLoad.add(onLoad);
    }
}
