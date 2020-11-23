package com.oop.datamodule.testing;

import com.oop.datamodule.api.StorageRegistry;
import com.oop.datamodule.universal.UniversalStorage;
import lombok.NonNull;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ObjectStorage extends UniversalStorage<Object> {
    private Map<UUID, Object> data = new ConcurrentHashMap<>();

    public ObjectStorage(
            @NonNull StorageRegistry storageRegistry
    ) {
        super(storageRegistry);
        addVariant("players", Object.class);
    }

    @Override
    protected void onAdd(Object object) {
        data.put(object.getUuid(), object);
    }

    @Override
    protected void onRemove(Object object) {
        data.remove(object.getUuid());
    }

    @Override
    public Stream<Object> stream() {
        return data.values().stream();
    }

    @Override
    public Iterator<Object> iterator() {
        return data.values().iterator();
    }
}
