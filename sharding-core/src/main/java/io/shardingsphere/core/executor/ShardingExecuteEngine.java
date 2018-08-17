/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.executor;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Sharding execute engine.
 * 
 * @author zhangliang
 */
public final class ShardingExecuteEngine implements AutoCloseable {
    
    private static final ExecutorService SHUTDOWN_EXECUTOR = Executors.newSingleThreadExecutor(ShardingThreadFactoryBuilder.build("Executor-Engine-Closer"));
    
    @Getter
    private final ListeningExecutorService executorService;
    
    public ShardingExecuteEngine(final int executorSize) {
        executorService = MoreExecutors.listeningDecorator(
                0 == executorSize ? Executors.newCachedThreadPool(ShardingThreadFactoryBuilder.build()) : Executors.newFixedThreadPool(executorSize, ShardingThreadFactoryBuilder.build()));
        MoreExecutors.addDelayedShutdownHook(executorService, 60, TimeUnit.SECONDS);
    }
    
    /**
     * Execute all callbacks.
     *
     * @param inputs input values
     * @param callback sharding execute callback
     * @param <I> type of input value
     * @param <O> type of return value
     * @return execute result
     * @throws Exception throw if execute failure
     */
    public <I, O> List<O> execute(final Collection<I> inputs, final ShardingExecuteCallback<I, O> callback) throws Exception {
        if (inputs.isEmpty()) {
            return Collections.emptyList();
        }
        Iterator<I> inputIterator = inputs.iterator();
        I firstInput = inputIterator.next();
        Collection<ListenableFuture<O>> restFutures = asyncExecute(Lists.newArrayList(inputIterator), callback);
        return getResults(callback.execute(firstInput), restFutures);
    }
    
    private <I, O> Collection<ListenableFuture<O>> asyncExecute(final Collection<I> inputs, final ShardingExecuteCallback<I, O> callback) {
        Collection<ListenableFuture<O>> result = new ArrayList<>(inputs.size());
        for (final I each : inputs) {
            result.add(executorService.submit(new Callable<O>() {
                
                @Override
                public O call() throws Exception {
                    return callback.execute(each);
                }
            }));
        }
        return result;
    }
    
    private <O> List<O> getResults(final O firstResult, final Collection<ListenableFuture<O>> restFutures) throws ExecutionException, InterruptedException {
        List<O> result = new LinkedList<>();
        result.add(firstResult);
        for (ListenableFuture<O> each : restFutures) {
            result.add(each.get());
        }
        return result;
    }
    
    /**
     * execute all callbacks for group.
     *
     * @param inputs input value's map
     * @param callback sharding execute callback
     * @param <I> type of input value
     * @param <O> type of return value
     * @return execute result
     * @throws Exception throw if execute failure
     */
    public <I, O> List<O> groupExecute(final Map<String, Collection<I>> inputs, final ShardingExecuteCallback<I, O> callback) throws Exception {
        if (inputs.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<I> firstInputs = inputs.remove(inputs.keySet().iterator().next());
        Collection<ListenableFuture<Collection<O>>> restResultFutures = asyncGroupExecute(inputs, callback);
        return getGroupResults(doGroupExecute(firstInputs, callback), restResultFutures);
    }
    
    private <I, O> Collection<ListenableFuture<Collection<O>>> asyncGroupExecute(final Map<String, Collection<I>> inputs, final ShardingExecuteCallback<I, O> callback) {
        Collection<ListenableFuture<Collection<O>>> result = new ArrayList<>(inputs.size());
        for (final Collection<I> each : inputs.values()) {
            result.add(executorService.submit(new Callable<Collection<O>>() {
                
                @Override
                public Collection<O> call() throws Exception {
                    return doGroupExecute(each, callback);
                }
            }));
        }
        return result;
    }
    
    private <I, O> Collection<O> doGroupExecute(final Collection<I> input, final ShardingExecuteCallback<I, O> callback) throws Exception {
        Collection<O> result = new LinkedList<>();
        for (I each : input) {
            result.add(callback.execute(each));
        }
        return result;
    }
    
    private <O> List<O> getGroupResults(final Collection<O> firstResults, final Collection<ListenableFuture<Collection<O>>> restFutures) throws ExecutionException, InterruptedException {
        List<O> result = new LinkedList<>();
        result.addAll(firstResults);
        for (ListenableFuture<Collection<O>> each : restFutures) {
            result.addAll(each.get());
        }
        return result;
    }
    
    @Override
    public void close() {
        SHUTDOWN_EXECUTOR.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    executorService.shutdown();
                    while (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
