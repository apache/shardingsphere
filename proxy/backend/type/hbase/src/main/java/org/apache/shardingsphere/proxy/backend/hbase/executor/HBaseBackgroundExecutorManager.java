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

import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;

import java.io.Closeable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * HBase background executor manager.
 */
public final class HBaseBackgroundExecutorManager implements Closeable {
    
    private final ScheduledExecutorService executorService;
    
    public HBaseBackgroundExecutorManager() {
        executorService = Executors.newScheduledThreadPool(1, ExecutorThreadFactoryBuilder.build("background"));
    }
    
    /**
     * Submit task.
     * 
     * @param runnable task
     * @param interval running interval
     */
    public void submit(final Runnable runnable, final int interval) {
        executorService.scheduleWithFixedDelay(runnable, interval, interval, TimeUnit.SECONDS);
    }
    
    @Override
    public void close() {
        executorService.shutdown();
    }
}
