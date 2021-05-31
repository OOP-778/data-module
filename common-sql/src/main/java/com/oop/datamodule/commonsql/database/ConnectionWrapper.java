package com.oop.datamodule.commonsql.database;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class ConnectionWrapper {
  @NonNull private final Connection connection;
  @NonNull private final Consumer<Connection> onEvict;

  public ConnectionWrapper use(ThrowableConsumer<Connection> user) {
    synchronized (connection) {
      try {
        user.accept(connection);
        return this;
      } catch (Throwable throwable) {
        throw new IllegalStateException("Failed to use connection", throwable);
      } finally {
        evict();
      }
    }
  }

  public ConnectionWrapper useNonLock(ThrowableConsumer<Connection> user) {
    try {
      user.accept(connection);
      return this;
    } catch (Throwable throwable) {
      throw new IllegalStateException("Failed to use connection", throwable);
    } finally {
      evict();
    }
  }

  public <T> T provideAndEvict(Function<Connection, T> provider) {
    synchronized (connection) {
      try {
        return provider.apply(connection);
      } catch (Throwable throwable) {
        throw new IllegalStateException("Failed to use connection", throwable);
      } finally {
        evict();
      }
    }
  }

  public void evict() {
    onEvict.accept(connection);
  }
}
