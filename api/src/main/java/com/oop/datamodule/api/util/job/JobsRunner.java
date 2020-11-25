package com.oop.datamodule.api.util.job;

import lombok.SneakyThrows;

import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class JobsRunner {
    private final Object lock = true;

    private final ExecutorService executor;
    private final Set<NativeJob> jobs = ConcurrentHashMap.newKeySet();
    private JobsResult result;
    private final CompletableFuture<JobsResult> completionFuture = new CompletableFuture<>();

    public JobsRunner(ExecutorService executor) {
        this.executor = executor;
    }

    public static JobsRunner acquire() {
        return new JobsRunner(
                Executors.newCachedThreadPool()
        );
    }

    @SneakyThrows
    protected void onCompletion(NativeJob job) {
        synchronized (lock) {
            jobs.remove(job);

            if (result == null)
                result = new JobsResult();

            result.getCompleted().incrementAndGet();
            if (job.resultedTo != null)
                result.getErrors().add(job.resultedTo);

            if (jobs.isEmpty()) {
                completionFuture.complete(result);

                executor.shutdownNow();
                executor.awaitTermination(10, TimeUnit.SECONDS);
            }
        }
    }

    public JobsRunner addJob(Job job) {
        NativeJob nativeJob = new NativeJob(job, this::onCompletion);
        jobs.add(nativeJob);
        return this;
    }

    public void startAndForget() {
        for (NativeJob job : jobs)
            executor.execute(job);
    }

    @SneakyThrows
    public JobsResult startAndWait() {
        if (jobs.isEmpty()) return new JobsResult();

        for (NativeJob job : jobs)
            executor.submit(job);

        return completionFuture.get(5, TimeUnit.MINUTES);
    }

    protected static class NativeJob extends Job {
        private final Job passed;
        private final Consumer<NativeJob> completion;
        private Throwable resultedTo;

        public NativeJob(Job passed, Consumer<NativeJob> completion) {
            this.passed = passed;
            this.completion = completion;
        }

        @Override
        public String getName() {
            return passed.getName();
        }

        @Override
        public void run() {
            try {
                passed.run();
            } catch (Throwable throwable) {
                resultedTo = throwable;
            }
            completion.accept(this);
        }
    }

}
