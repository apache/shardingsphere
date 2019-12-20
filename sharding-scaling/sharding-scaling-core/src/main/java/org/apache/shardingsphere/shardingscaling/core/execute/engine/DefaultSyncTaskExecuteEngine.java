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
import org.apache.shardingsphere.shardingscaling.core.execute.executor.SyncRunner;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.SyncRunnerGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Default implement for sync task execute engine.
 *
 * @author avalon566
 * @author yangyi
 */
public final class DefaultSyncTaskExecuteEngine implements SyncTaskExecuteEngine {
    
    private final ListeningExecutorService executorService;
    
    private int availableWorkerThread;
    
    public DefaultSyncTaskExecuteEngine(final int maxWorkerThread) {
        executorService = MoreExecutors.listeningDecorator(
            new ThreadPoolExecutor(maxWorkerThread, maxWorkerThread, 0, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy()));
        availableWorkerThread = maxWorkerThread;
    }
    
    @Override
    public void submitGroup(final SyncRunnerGroup syncRunnerGroup) {
        Iterable<ListenableFuture<Object>> listenableFutures = submit(syncRunnerGroup.getSyncRunners());
        ListenableFuture allListenableFuture = Futures.allAsList(listenableFutures);
        Futures.addCallback(allListenableFuture, new FutureCallback<List<Object>>() {
        
            @Override
            public void onSuccess(final List<Object> result) {
                syncRunnerGroup.onSuccess();
            }
        
            @Override
            public void onFailure(final Throwable t) {
                syncRunnerGroup.onFailure(t);
            }
        });
    }
    
    @Override
    public synchronized List<ListenableFuture<Object>> submit(final Collection<SyncRunner> syncRunners) {
        if (null == syncRunners || 0 == syncRunners.size()) {
            return Collections.emptyList();
        }
        if (availableWorkerThread < syncRunners.size()) {
            throw new RejectedExecutionException("The execute engine does not have enough threads to execute sync runner.");
        }
        List<ListenableFuture<Object>> result = new ArrayList<>(syncRunners.size());
        availableWorkerThread -= syncRunners.size();
        for (SyncRunner syncRunner : syncRunners) {
            ListenableFuture listenableFuture = executorService.submit(syncRunner);
            addReleaseWorkerThreadCallback(listenableFuture);
            result.add(listenableFuture);
        }
        return result;
    }
    
    private synchronized void releaseWorkerThread() {
        availableWorkerThread++;
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
}
