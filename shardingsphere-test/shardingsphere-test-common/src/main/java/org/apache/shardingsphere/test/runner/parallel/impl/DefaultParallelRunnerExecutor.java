package org.apache.shardingsphere.test.runner.parallel.impl;


import org.apache.shardingsphere.test.runner.parallel.ParallelRunnerExecutor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DefaultParallelRunnerExecutor implements ParallelRunnerExecutor {

    private final Collection<Future<?>> taskFeatures = new LinkedList<>();

    public ExecutorService getExecuteService(){
       return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void execute(final Runnable childStatement) {
        taskFeatures.add(getExecuteService().submit(childStatement));
    }

    @Override
    public void finished() {
        taskFeatures.forEach(each -> {
            try {
                each.get();
            } catch (final InterruptedException | ExecutionException ignored) {
            }
        });
        getExecuteService().shutdownNow();
    }
}
