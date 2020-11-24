package com.oop.datamodule.mongodb;

import com.oop.datamodule.api.util.job.Job;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class MongoJob extends Job {
    private @NonNull Runnable runnable;

    @Override
    public String getName() {
        return "mongo-job";
    }

    @Override
    public void run() {
        runnable.run();
    }
}
