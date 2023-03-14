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

package org.apache.shardingsphere.proxy.backend.hbase.executor;

import java.io.Closeable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * HBase task executor manager.
 */
public final class HBaseTaskExecutorManager implements Closeable {
    
    private final ThreadPoolExecutor executorService;
    
    /**
     * HBase task executor manager.
     * @param poolSize pool size
     */
    public HBaseTaskExecutorManager(final int poolSize) {
        executorService = new ThreadPoolExecutor(poolSize, poolSize, 10L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20000), new ThreadPoolExecutor.CallerRunsPolicy());
    }
    
    /**
     * Submit task.
     * 
     * @param runnable task
     */
    public void submit(final Runnable runnable) {
        executorService.submit(runnable);
    }
    
    @Override
    public void close() {
        executorService.shutdown();
    }
    
}
