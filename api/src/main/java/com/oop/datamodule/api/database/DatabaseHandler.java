package com.oop.datamodule.api.database;

import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.util.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This links everything between memory and database This can be used not only on storages, but as
 * self wrapper
 */
public interface DatabaseHandler<S extends DatabaseStructure, T extends DatabaseHandler<S, T>> {

  /** Remove data from the database */
  void remove(@NonNull S structure, @NonNull ObjectIdentifier data);

  /** Update database structure. Only used on SQL based databases */
  void updateStructure(@NonNull S structure);

  /** Grab single value from the database * */
  SerializedData grabData(@NonNull S structure, @NonNull GrabData grabData);

  /** Update one or multiple values */
  void updateOrInsertData(@NonNull S structure, @NonNull UpdateData updateData);

  /** Check if a value exists by an identifier */
  boolean exists(@NonNull S structure, @NonNull ObjectIdentifier data);

  /** If the handler supports partial updates */
  boolean supportsPartialUpdates();

  @Getter
  class UpdateData extends ObjectIdentifier {

    private @NonNull Map<String, SerializedData> updatingColumns;

    public UpdateData(
        String keyIdentifier,
        SerializedData objectThatIdentifies,
        Map<String, SerializedData> updatingColumns) {
      super(keyIdentifier, objectThatIdentifies);
      this.updatingColumns = updatingColumns;
    }

    public static class UpdateDataBuilder {
      private final @NonNull Map<String, SerializedData> updatingColumns = new HashMap<>();

      public UpdateDataBuilder addColumn(String key, SerializedData data) {
        updatingColumns.put(key, data);
        return this;
      }
    }
  }

  @Getter
  class GrabData extends ObjectIdentifier {
    private @NonNull final String[] grabbing;

    public GrabData(
        String keyIdentifier,
        SerializedData objectThatIdentifies,
        String[] grabbing) {
      super(keyIdentifier, objectThatIdentifies);
      this.grabbing = grabbing;
      Preconditions.checkArgument(grabbing.length != 0, "Cannot grab zero columns!");
    }

    public static class GrabDataBuilder {
      private @NonNull String[] grabbing = new String[0];

      public GrabDataBuilder appendColumn(String... columns) {
        this.grabbing =
            Stream.concat(Arrays.stream(grabbing), Arrays.stream(columns)).toArray(String[]::new);
        return this;
      }
    }
  }

  @Builder
  @Getter
  @AllArgsConstructor
  class ObjectIdentifier {
    private @NonNull final String keyIdentifier;
    private @NonNull final SerializedData objectThatIdentifies;
  }
}
