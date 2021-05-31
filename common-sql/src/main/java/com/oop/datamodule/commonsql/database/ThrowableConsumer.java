package com.oop.datamodule.commonsql.database;

@FunctionalInterface
public interface ThrowableConsumer<T> {

  void accept(T object) throws Throwable;
}
