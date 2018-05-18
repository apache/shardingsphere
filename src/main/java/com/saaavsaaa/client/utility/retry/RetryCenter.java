package com.saaavsaaa.client.utility.retry;

import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.DelayQueue;

/**
 * Created by aaa
 */
public enum RetryCenter {
    INSTANCE;
    
    private static final Logger logger = LoggerFactory.getLogger(RetryCenter.class);
    private final DelayQueue<BaseOperation> queue = new DelayQueue<>();
    private final RetryThread retryThread = new RetryThread(queue);
    
    private boolean started = false;
    private DelayRetry retrial;
    
    public void init(DelayRetry retrial) {
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
        retryThread.setName("retry-thread");
        retryThread.start();
    }
    
    public void add(BaseOperation operation){
        if (retrial == null){
            logger.debug("retrial no init");
            retrial = DelayRetry.newNoInitDelayRetrial();
        }
        operation.setRetrial(new DelayRetryExecution(retrial));
        queue.offer(operation);
        logger.debug("enqueue operation:{}", operation.toString());
    }
}
