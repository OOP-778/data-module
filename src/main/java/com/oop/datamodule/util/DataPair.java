package com.oop.datamodule.util;

import lombok.Getter;

@Getter
public class DataPair<K, V> {
    private K key;
    private V value;

    public DataPair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
