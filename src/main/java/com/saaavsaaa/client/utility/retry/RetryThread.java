package com.saaavsaaa.client.utility.retry;

import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by aaa
 */
public class RetryThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(RetryThread.class);
    private final ThreadPoolExecutor retryExecution;
    private final int corePoolSize = Runtime.getRuntime().availableProcessors();
    private final int maximumPoolSize = corePoolSize;
    private final long keepAliveTime = 0;
    private final int closeDelay = 60;
    private final DelayQueue<BaseOperation> queue;
    
    public RetryThread(DelayQueue<BaseOperation> queue) {
        this.queue = queue;
        retryExecution = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(10), new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("zk-retry-" + threadIndex.incrementAndGet());
                return thread;
            }
        });
        addDelayedShutdownHook(retryExecution, closeDelay, TimeUnit.SECONDS);
    }

    @Override
    public void run(){
        logger.debug("RetryThread start");
        for (;;) {
            BaseOperation operation;
            try {
                operation = queue.take();
                logger.debug("take operation:{}", operation.toString());
            } catch (InterruptedException e) {
                logger.error("retry interrupt e:{}", e.getMessage());
                continue;
            }
            retryExecution.submit(new Runnable() {
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
                    logger.debug("RetryCenter stop");
                    queue.clear();
                    service.shutdown();
                    service.awaitTermination(terminationTimeout, timeUnit);
                } catch (InterruptedException ignored) {
                    // We're shutting down anyway, so just ignore.
                }
            }
        });
        thread.setName("retry shutdown hook");
        Runtime.getRuntime().addShutdownHook(thread);
    }
}
