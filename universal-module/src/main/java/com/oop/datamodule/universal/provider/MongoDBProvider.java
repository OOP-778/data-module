package com.oop.datamodule.universal.provider;

import com.mongodb.client.MongoDatabase;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.mongodb.MongoCredential;
import com.oop.datamodule.mongodb.storage.MongoDBStorage;
import com.oop.datamodule.universal.Linker;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import lombok.NonNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MongoDBProvider implements StorageProvider<MongoCredential> {
    @Override
    public <B extends UniversalBodyModel> Storage<B> provide(Linker<B> linker, MongoCredential credential) {
        return new MongoStorageImpl<>(linker, credential.build());
    }

    protected static class MongoStorageImpl<T extends UniversalBodyModel> extends MongoDBStorage<T> {
        private final Linker<T> linker;

        public MongoStorageImpl(@NonNull Linker<T> linker, @NonNull MongoDatabase database) {
            super(database);
            this.linker = linker;
        }

        @Override
        protected void onAdd(T object) {
            linker.onAdd(object);
        }

        @Override
        protected void onRemove(T object) {
            linker.onRemove(object);
        }

        @Override
        public Stream<T> stream() {
            return linker.getStorage().stream();
        }

        @Override
        public Iterator<T> iterator() {
            return linker.getStorage().iterator();
        }

        @Override
        public Map<String, Class<T>> getVariants() {
            return linker.getVariants();
        }

        @Override
        protected List<Consumer<Storage<T>>> getOnLoad() {
            return linker.getOnLoad();
        }
    }
}
