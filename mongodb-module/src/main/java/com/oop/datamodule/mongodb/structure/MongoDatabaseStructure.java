package com.oop.datamodule.mongodb.structure;

import com.oop.datamodule.api.database.DatabaseStructure;
import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class MongoDatabaseStructure implements DatabaseStructure {

    private @NonNull
    final String collectionName;
    private final String uniqueFieldName;

}
