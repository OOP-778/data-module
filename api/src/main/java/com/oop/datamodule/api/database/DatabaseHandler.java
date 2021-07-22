package com.oop.datamodule.api.database;

import com.oop.datamodule.api.SerializedData;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * This links everything between memory and database This can be used not only on storages, but as
 * self wrapper
 */
public interface DatabaseHandler {

  /** Remove data from the database */
  void remove(@NonNull DatabaseStructure structure, @NonNull ObjectIdentifier data);

  /** Update database structure. Only used on SQL based databases */
  void updateStructure(@NonNull DatabaseStructure structure);

  /** Grab single value from the database * */
  SerializedData grabData(@NonNull DatabaseStructure structure, @NonNull GrabData grabData);

  /** Update one or multiple values */
  void updateOrInsertData(@NonNull DatabaseStructure structure, @NonNull UpdateData updateData);

  /** Check if a value exists by an identifier */
  boolean exists(@NonNull DatabaseStructure structure, @NonNull ObjectIdentifier data);

  /** If the handler supports partial updates */
  boolean supportsPartialUpdates();

  /** Identifier of the database type */
  String identifier();

  @Getter
  @SuperBuilder(toBuilder = true)
  class UpdateData extends ObjectIdentifier {

    @Singular("property")
    private final @NonNull Map<String, SerializedData> properties;
  }

  @Getter
  @SuperBuilder(toBuilder = true)
  class ObjectIdentifier {
    private @NonNull final String keyIdentifier;
    private @NonNull final SerializedData objectThatIdentifies;
  }

  @SuperBuilder(toBuilder = true)
  @Getter
  class GrabData extends ObjectIdentifier {
    @Singular("grab")
    private @NonNull final List<String> grabbing;
  }
}
