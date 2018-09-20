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

package io.shardingsphere.orchestration.reg.newzk.client.retry;

import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base.BaseOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Async retry.
 *
 * @author lidongbo
 */
@Slf4j
public final class RetryThread extends Thread {
    
    private final int corePoolSize = Runtime.getRuntime().availableProcessors();
    
    private final int maximumPoolSize = corePoolSize;
    
    private final long keepAliveTime = 0;
    
    private final int closeDelay = 60;
    
    private final DelayQueue<BaseOperation> queue;
    
    private final ThreadPoolExecutor retryExecutor;
    
    public RetryThread(final DelayQueue<BaseOperation> queue) {
        this.queue = queue;
        retryExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(10), new ThreadFactory() {
            
            private final AtomicInteger threadIndex = new AtomicInteger(0);
            
            @Override
            public Thread newThread(final Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.setName("zk-retry-" + threadIndex.incrementAndGet());
                return thread;
            }
        });
        addDelayedShutdownHook(retryExecutor, closeDelay, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        for (;;) {
            final BaseOperation operation;
            try {
                operation = queue.take();
            } catch (final InterruptedException ex) {
                log.error("retry interrupt ex: {}", ex.getMessage());
                continue;
            }
            retryExecutor.submit(new Runnable() {
                
                @Override
                public void run() {
                    boolean result;
                    try {
                        result = operation.executeOperation();
                    } catch (final KeeperException | InterruptedException ex) {
                        result = false;
                        log.error("retry disrupt operation: {}, ex: {}", operation.toString(), ex.getMessage());
                    }
                    if (result) {
                        queue.offer(operation);
                    }
                }
            });
        }
    }
    
    private void addDelayedShutdownHook(final ExecutorService service, final long terminationTimeout, final TimeUnit timeUnit) {
        Thread thread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    queue.clear();
                    service.shutdown();
                    service.awaitTermination(terminationTimeout, timeUnit);
                } catch (final InterruptedException ignored) {
                }
            }
        });
        thread.setName("retry shutdown hook");
        Runtime.getRuntime().addShutdownHook(thread);
    }
}
