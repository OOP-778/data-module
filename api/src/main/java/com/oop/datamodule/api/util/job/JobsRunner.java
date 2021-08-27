package com.oop.datamodule.api.util.job;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;

public class JobsRunner {
    private final Set<CompletableFuture<Void>> jobs = ConcurrentHashMap.newKeySet();
    private final Set<Throwable> errors = ConcurrentHashMap.newKeySet();

    protected JobsRunner() {
    }

    public static JobsRunner acquire() {
        return new JobsRunner();
    }

    public JobsRunner addJob(Job job) {
        final CompletableFuture<Void> future = CompletableFuture
            .runAsync(job);
        future.whenComplete(($, error) -> {
            if (error == null) {
                return;
            }
            this.errors.add(error);
        });
        this.jobs.add(future);
        return this;
    }

    @SneakyThrows
    public JobsResult startAndWait() {
        if (jobs.isEmpty()) {
            return new JobsResult(errors, new AtomicInteger(0));
        }

        CompletableFuture
            .allOf(jobs.toArray(new CompletableFuture[0]))
            .get(1, TimeUnit.MINUTES);
        return new JobsResult(errors, new AtomicInteger(jobs.size() - errors.size()));
    }
}
