package com.oop.datamodule.testing;

import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class Object implements UniversalBodyModel {
    private UUID uuid;

    private SubObject object = new SubObject();

    private List<Integer> counts = new ArrayList<>();

    private List<SubObject> subObjects = new ArrayList<>();

    @Setter
    private int coins = 0;

    public Object(UUID uuid) {
        this.uuid = uuid;
    }

    private Object() {}

    @Override
    public void serialize(SerializedData data) {
        data.write("uuid", uuid);
        data.write("coins", coins);
        data.write("sub", object);
        data.write("counts", counts);
        data.write("subObjects", subObjects);
    }

    @Override
    public void deserialize(SerializedData data) {
        this.uuid = data.applyAs("uuid", UUID.class);
        this.coins = data.applyAs("coins", int.class);
        this.object = data.applyAs("sub", SubObject.class, SubObject::new);
        if (data.has("counts"))
            this.counts = data
                    .applyAsCollection("counts")
                    .map(sd -> sd.applyAs(int.class))
                    .collect(Collectors.toList());

        if (data.has("subObjects"))
            this.subObjects = data
                    .applyAsCollection("subObjects")
                    .map(sd -> sd.applyAs(SubObject.class))
                    .collect(Collectors.toList());
    }

    @Override
    public String getKey() {
        return uuid.toString();
    }

    @Override
    public void save(boolean async, Runnable callback) {
        // Get instance of ur storage and use save(this, async callback);
    }

    // For SQL based databases
    @Override
    public String[] getStructure() {
        return new String[]{
                "uuid",
                "coins"
        };
    }

    @Override
    public String toString() {
        return "Object{" +
                "uuid=" + uuid +
                ", coins=" + coins +
                '}';
    }

    // For mongoDB
    @Override
    public String getIdentifierKey() {
        return "uuid";
    }
}
