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

package io.shardingsphere.jdbc.orchestration.reg.new_zk.client.retry;

import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.base.BaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * async retry
 *
 * @author lidongbo
 */
public class RetryThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(RetryThread.class);
    private final ThreadPoolExecutor retryExecutor;
    private final int corePoolSize = Runtime.getRuntime().availableProcessors();
    private final int maximumPoolSize = corePoolSize;
    private final long keepAliveTime = 0;
    private final int closeDelay = 60;
    private final DelayQueue<BaseOperation> queue;
    
    public RetryThread(DelayQueue<BaseOperation> queue) {
        this.queue = queue;
        retryExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(10), new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("zk-retry-" + threadIndex.incrementAndGet());
                logger.debug("new thread:{}", thread.getName());
                return thread;
            }
        });
        addDelayedShutdownHook(retryExecutor, closeDelay, TimeUnit.SECONDS);
    }

    @Override
    public void run(){
        logger.debug("RetryThread start");
        for (;;) {
            final BaseOperation operation;
            try {
                operation = queue.take();
                logger.debug("take operation:{}", operation.toString());
            } catch (InterruptedException e) {
                logger.error("retry interrupt e:{}", e.getMessage());
                continue;
            }
            retryExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    boolean result;
                    try {
                        result = operation.executeOperation();
                    } catch (Exception e) {
                        result = false;
                        logger.error("retry disrupt operation:{}, e:{}", operation.toString(), e.getMessage());
                    }
                    if (result){
                        queue.offer(operation);
                        logger.debug("enqueue again operation:{}", operation.toString());
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
                    logger.debug("AsyncRetryCenter stop");
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
