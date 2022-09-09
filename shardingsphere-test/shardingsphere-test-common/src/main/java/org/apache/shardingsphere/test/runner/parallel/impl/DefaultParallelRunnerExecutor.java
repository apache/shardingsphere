package org.apache.shardingsphere.test.runner.parallel.impl;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.shardingsphere.test.runner.parallel.ParallelRunnerExecutor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DefaultParallelRunnerExecutor<T> implements ParallelRunnerExecutor<T> {

    protected final Collection<Future<?>> taskFeatures = new LinkedList<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-ParallelTestThread-%d").build());


    @Override
    public void execute(T key, Runnable childStatement) {

    }

    @Override
    public void execute(final Runnable childStatement) {
        taskFeatures.add(executorService.submit(childStatement));
    }

    @Override
    public void finished() {
        taskFeatures.forEach(each -> {
            try {
                each.get();
            } catch (final InterruptedException | ExecutionException ignored) {
            }
        });
        executorService.shutdownNow();
    }
}
