package com.saaavsaaa.client.retry;

import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.DelayQueue;

/**
 * Created by aaa
 */
public enum AsyncRetryCenter {
    INSTANCE;
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncRetryCenter.class);
    private final DelayQueue<BaseOperation> queue = new DelayQueue<>();
    private final RetryThread retryThread = new RetryThread(queue);
    
    private boolean started = false;
    private RetryPolicy retryPolicy;
    
    public void init(RetryPolicy retryPolicy) {
        logger.debug("retryPolicy init");
        if (retryPolicy == null){
            logger.warn("retryPolicy is null and auto init with RetryPolicy.newNoInitDelayPolicy");
            retryPolicy = RetryPolicy.newNoInitDelayPolicy();
        }
        this.retryPolicy = retryPolicy;
    }
    
    public synchronized void start(){
        if (started){
            return;
        }
        this.started = true;
        retryThread.setName("retry-thread");
        retryThread.start();
    }
    
    public void add(BaseOperation operation){
        if (retryPolicy == null){
            logger.warn("retryPolicy no init and auto init with RetryPolicy.newNoInitDelayPolicy");
            retryPolicy = RetryPolicy.newNoInitDelayPolicy();
        }
        operation.setRetrial(new DelayRetryExecution(retryPolicy));
        queue.offer(operation);
        logger.debug("enqueue operation:{}", operation.toString());
    }
}
