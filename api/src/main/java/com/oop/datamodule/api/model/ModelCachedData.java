package com.oop.datamodule.api.model;

import java.util.HashMap;
import java.util.Map;

public class ModelCachedData {
    private final Map<String, Integer> cache = new HashMap<>();

    public void clear() {
        cache.clear();
    }

    public void add(String field, String data) {
        cache.put(field, data.hashCode());
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public boolean isUpdated(String field, String newData) {
        Integer hash = cache.get(field);
        if (hash == null) {
            cache.put(field, newData.hashCode());
            return true;
        }
        if (hash == newData.hashCode()) return false;

        cache.remove(field);
        cache.put(field, newData.hashCode());
        return true;
    }
}
