package com.oop.datamodule.property.impl.key;

import com.oop.datamodule.api.util.Preconditions;
import com.oop.datamodule.property.api.key.KeyRegistry;
import com.oop.datamodule.property.api.key.PropertyKey;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.BiFunction;

public class ImplKeyRegistry implements KeyRegistry {

  private final ImplKeyRegistry parent;
  private final String key;
  private final Map<String, PropertyKey> keys = new TreeMap<>(String::compareToIgnoreCase);
  private final Map<String, ImplKeyRegistry> keyRegistryMap =
      new TreeMap<>(String::compareToIgnoreCase);

  @Setter @Getter private boolean isMutable = false;

  public ImplKeyRegistry(String key) {
    this(key, null);
  }

  public ImplKeyRegistry(String key, ImplKeyRegistry parent) {
    this.key = key;
    this.parent = parent;
  }

  @Override
  public Optional<KeyRegistry> parent() {
    return Optional.ofNullable(parent);
  }

  @Override
  public Optional<String> key() {
    return Optional.ofNullable(key);
  }

  protected PropertyKey _getKey(String value, boolean throwErrors) {
    final String[] split = value.split(":");
    final Queue<String> parents = new LinkedList<>(Arrays.asList(split[0].split("_")));

    if (parents.size() == 1 && split.length == 1) {
      PropertyKey propertyKey = keys.get(split[0]);
      if (propertyKey == null && isMutable) {
        return new ImplPropertyKey(this, split[0]).apply(key -> this.keys.put(split[0], key));
      }

      if (!throwErrors) return propertyKey;
      return Objects.requireNonNull(
              propertyKey,
              "Failed to find key in registry: "
                      + (key == null ? "default" : key)
                      + " of key: "
                      + split[0]);
    }

    Preconditions.checkArgument(split.length == 2, "KEY is invalid! The format is key_child:value");
    Optional<ImplKeyRegistry> implKeyRegistry = _getKeyRegistry(parents, isMutable);
    Preconditions.checkArgument(
            implKeyRegistry.isPresent(), "KEY is invalid! Failed to find parents " + parents.peek());

    return implKeyRegistry.get().getKey(split[1]);
  }

  @Override
  public PropertyKey getKey(String value) {
    return _getKey(value, true);
  }

  @Override
  public Optional<PropertyKey> getOptionalKey(String key) {
    return Optional.ofNullable(_getKey(key, false));
  }

  @Override
  public Optional<KeyRegistry> getRegistry(String key) {
    return Optional.empty();
  }

  protected Optional<ImplKeyRegistry> _getKeyRegistry(
      Queue<String> parents, boolean createIfNotPresent) {
    // Shouldn't happen
    if (parents.isEmpty()) return Optional.empty();

    ImplKeyRegistry currentRegistry = this;
    final BiFunction<String, ImplKeyRegistry, ImplKeyRegistry> keyRegistryProvider =
        (input, registry) -> {
          ImplKeyRegistry implKeyRegistry = registry.keyRegistryMap.get(input);
          if (implKeyRegistry == null && createIfNotPresent) {
            implKeyRegistry = new ImplKeyRegistry(input, registry);
            implKeyRegistry.setMutable(isMutable);

            registry.keyRegistryMap.put(input, implKeyRegistry);
          }

          return implKeyRegistry;
        };

    while (!parents.isEmpty()
        && (currentRegistry = keyRegistryProvider.apply(parents.peek(), currentRegistry)) != null) {
      parents.poll();
    }

    return Optional.ofNullable(currentRegistry);
  }

  public void registerParents(String... keys) {
    for (String key : keys) {
      _getKeyRegistry(new LinkedList<>(Arrays.asList(key.split("_"))), true);
    }
  }

  public void registerKeys(String... keys) {
    boolean wasMutable = isMutable();
    if (!wasMutable) {
      setMutable(true);
    }

    for (String s : keys) {
      getKey(s);
    }

    if (!wasMutable) {
      setMutable(false);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ImplKeyRegistry that = (ImplKeyRegistry) o;
    return Objects.equals(this.key, that.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.key);
  }
}
