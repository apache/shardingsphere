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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.BaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * async retry
 *
 * @author lidongbo
 */
public class RetryThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryThread.class);
    
    private final int corePoolSize = Runtime.getRuntime().availableProcessors();
    
    private final ThreadPoolExecutor retryExecutor;
    
    private final int maximumPoolSize = corePoolSize;
    
    private final long keepAliveTime = 0;
    
    private final int closeDelay = 60;
    
    private final DelayQueue<BaseOperation> queue;
    
    public RetryThread(final DelayQueue<BaseOperation> queue) {
        this.queue = queue;
        retryExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(10), new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(final Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("zk-retry-" + threadIndex.incrementAndGet());
                LOGGER.debug("new thread:{}", thread.getName());
                return thread;
            }
        });
        addDelayedShutdownHook(retryExecutor, closeDelay, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        LOGGER.debug("RetryThread start");
        for (;;) {
            final BaseOperation operation;
            try {
                operation = queue.take();
                LOGGER.debug("take operation:{}", operation.toString());
            } catch (InterruptedException e) {
                LOGGER.error("retry interrupt e:{}", e.getMessage());
                continue;
            }
            retryExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    boolean result;
                    try {
                        result = operation.executeOperation();
                        // CHECKSTYLE:OFF
                    } catch (Exception e) {
                        // CHECKSTYLE:ON
                        result = false;
                        LOGGER.error("retry disrupt operation:{}, e:{}", operation.toString(), e.getMessage());
                    }
                    if (result) {
                        queue.offer(operation);
                        LOGGER.debug("enqueue again operation:{}", operation.toString());
                    }
                }
            });
        }
    }
    
    final void addDelayedShutdownHook(final ExecutorService service, final long terminationTimeout, final TimeUnit timeUnit) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LOGGER.debug("AsyncRetryCenter stop");
                    queue.clear();
                    service.shutdown();
                    service.awaitTermination(terminationTimeout, timeUnit);
                } catch (InterruptedException ignored) {
                    // shutting down anyway, just ignore.
                }
            }
        });
        thread.setName("retry shutdown hook");
        Runtime.getRuntime().addShutdownHook(thread);
    }
}
