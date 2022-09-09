package org.apache.shardingsphere.test.runner.parallel;

import org.apache.shardingsphere.test.runner.parallel.annotaion.ParallelLevel;

import java.util.Collection;


public interface ParallelRunnerExecutorFactory<T> {

    public ParallelRunnerExecutor getExecutor(final T key, final ParallelLevel parallelLevel);

    public ParallelRunnerExecutor getExecutor(final ParallelLevel parallelLevel);

    /**
     * Get all executors.
     *
     * @return all executors
     */
    public Collection<ParallelRunnerExecutor> getAllExecutors();
}
