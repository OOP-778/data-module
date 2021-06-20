package com.oop.datamodule.property.key;

import com.oop.datamodule.property.api.key.KeyRegistry;
import com.oop.datamodule.property.api.key.PropertyKey;
import lombok.NonNull;

import java.util.Objects;
import java.util.Optional;

public class ImplPropertyKey implements PropertyKey {

  private final KeyRegistry parent;
  private final String value;

  public ImplPropertyKey(@NonNull KeyRegistry parent, @NonNull String value) {
      this.parent = parent;
      this.value = value;
  }

  @Override
  public KeyRegistry registry() {
    return parent;
  }

  @Override
  public Optional<String> parent() {
    return parent.key();
  }

  @Override
  public String identifier() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ImplPropertyKey that = (ImplPropertyKey) o;
    return Objects.equals(this.value, that.value) && Objects.equals(that.parent, this.parent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
