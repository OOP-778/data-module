package com.oop.datamodule.api.storage.lock;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModelLock<T> {
  @NonNull protected final T object;

  private final ReentrantLock lock = new ReentrantLock();

  public void lockAndUse(Consumer<T> consumer) {
    try {
      lock.lock();
      consumer.accept(object);
    } finally {
      lock.unlock();
    }
  }

  public void lockAndUse(Runnable runnable) {
    try {
      lock.lock();
      runnable.run();
    } finally {
      lock.unlock();
    }
  }

  public void lockAndUseSelf(Consumer<ModelLock<T>> lockConsumer) {
    try {
      lock.lock();
      lockConsumer.accept(this);
    } finally {
      lock.unlock();
    }
  }

  public boolean isLocked() {
    return lock.isLocked();
  }
}
