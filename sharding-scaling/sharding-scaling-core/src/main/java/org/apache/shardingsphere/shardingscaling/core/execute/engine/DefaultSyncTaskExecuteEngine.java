/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingscaling.core.execute.engine;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.SyncExecutor;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.SyncExecutorGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default implement for sync task execute engine.
 */
public final class DefaultSyncTaskExecuteEngine implements SyncTaskExecuteEngine {
    
    private final ListeningExecutorService executorService;
    
    private AtomicInteger availableWorkerThread;
    
    public DefaultSyncTaskExecuteEngine(final int maxWorkerThread) {
        executorService = MoreExecutors.listeningDecorator(
            new ThreadPoolExecutor(maxWorkerThread, maxWorkerThread, 0, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy()));
        availableWorkerThread = new AtomicInteger(maxWorkerThread);
    }
    
    @Override
    public void submitGroup(final SyncExecutorGroup syncExecutorGroup) {
        Iterable<ListenableFuture<Object>> listenableFutures = submit(syncExecutorGroup.getSyncExecutors());
        ListenableFuture allListenableFuture = Futures.allAsList(listenableFutures);
        Futures.addCallback(allListenableFuture, new FutureCallback<List<Object>>() {
        
            @Override
            public void onSuccess(final List<Object> result) {
                syncExecutorGroup.onSuccess();
            }
        
            @Override
            public void onFailure(final Throwable t) {
                syncExecutorGroup.onFailure(t);
            }
        });
    }
    
    @Override
    public synchronized List<ListenableFuture<Object>> submit(final Collection<SyncExecutor> syncExecutors) {
        if (null == syncExecutors || 0 == syncExecutors.size()) {
            return Collections.emptyList();
        }
        if (availableWorkerThread.get() < syncExecutors.size()) {
            throw new RejectedExecutionException("The execute engine does not have enough threads to execute sync executor.");
        }
        List<ListenableFuture<Object>> result = new ArrayList<>(syncExecutors.size());
        availableWorkerThread.addAndGet(-syncExecutors.size());
        for (SyncExecutor syncExecutor : syncExecutors) {
            ListenableFuture listenableFuture = executorService.submit(syncExecutor);
            addReleaseWorkerThreadCallback(listenableFuture);
            result.add(listenableFuture);
        }
        return result;
    }
    
    private void addReleaseWorkerThreadCallback(final ListenableFuture listenableFuture) {
        Futures.addCallback(listenableFuture, new FutureCallback() {
            
            @Override
            public void onSuccess(final Object r) {
                releaseWorkerThread();
            }
            
            @Override
            public void onFailure(final Throwable t) {
                releaseWorkerThread();
            }
        });
    }
    
    private synchronized void releaseWorkerThread() {
        availableWorkerThread.incrementAndGet();
    }
}
