package com.oop.datamodule.universal;

import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Allows linking of storage providers implementations to access onAdd and onRemove and more
 */
@AllArgsConstructor
@Getter
public class Linker<B extends UniversalBodyModel> {
    private final UniversalStorage<B> storage;

    public void onAdd(B object) {
        storage.getAdder().accept(object);
    }

    public void onRemove(B object) {
        storage.getRemover().accept(object);
    }

    public Map<String, Class<B>> getVariants() {
        return storage.getVariantsSupplier().get();
    }

    public List<Consumer<Storage<B>>> getOnLoad() {
        return storage.getOnLoad();
    }
}
