package com.saaavsaaa.client.utility.retry;

import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by aaa
 */
public enum RetrialCenter {
    INSTANCE;
    
    private static final Logger logger = LoggerFactory.getLogger(RetrialCenter.class);
    private final DelayQueue<BaseOperation> queue = new DelayQueue<>();
    private final ThreadPoolExecutor retryExecution;
    private final int corePoolSize = Runtime.getRuntime().availableProcessors();
    private final int maximumPoolSize = corePoolSize;
    private final long keepAliveTime = 0;
    private final int closeDelay = 60;
    
    private boolean started = false;
    private DelayRetrial retrial;
    
    private RetrialCenter(){
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
    
    public void init(DelayRetrial retrial) {
        logger.debug("retrial init");
        if (retrial == null) {
            logger.debug("retrial real init");
            this.retrial = retrial;
        }
    }
    
    public synchronized void start(){
        if (started){
            return;
        }
        this.started = true;
        logger.debug("RetrialCenter start");
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
                    started = false;
                    logger.debug("RetrialCenter stop");
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
    
    public void add(BaseOperation operation){
        if (retrial == null){
            logger.debug("retrial no init");
            retrial = DelayRetrial.newNoInitDelayRetrial();
        }
        operation.setRetrial(new DelayRetryExecution(retrial));
        queue.offer(operation);
        logger.debug("enqueue operation:{}", operation.toString());
    }
}
