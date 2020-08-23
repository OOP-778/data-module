package com.oop.datamodule;

import java.util.HashMap;
import java.util.Map;

public class BodyCache {
    private final Map<String, Integer> cache = new HashMap<>();

    public void add(String field, String data) {
        cache.put(field, data.hashCode());
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
