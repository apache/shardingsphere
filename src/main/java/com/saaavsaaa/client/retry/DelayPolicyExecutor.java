package com.saaavsaaa.client.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by aaa
 */
public class DelayPolicyExecutor {
    private static final Logger logger = LoggerFactory.getLogger(DelayPolicyExecutor.class);
    private final DelayRetryPolicy delayRetryPolicy;
    private final Random random;
    
    private int executeCount = 0;
    private long executeTick;
    
    public DelayPolicyExecutor(){
        this(DelayRetryPolicy.newNoInitDelayPolicy());
    }
    
    public DelayPolicyExecutor(final DelayRetryPolicy delayRetryPolicy) {
        this.delayRetryPolicy = delayRetryPolicy;
        this.executeTick = System.currentTimeMillis();
        this.random = new Random();
//        next();
    }
    
    public boolean hasNext() {
        return executeCount < delayRetryPolicy.getRetryCount();
    }
    
    public long getNextTick() {
        return executeTick;
    }
    
    public void next() {
        executeCount ++;
        long sleep = delayRetryPolicy.getBaseDelay() * Math.max(1, this.random.nextInt(1 << delayRetryPolicy.getRetryCount() + 1));
        if (sleep < delayRetryPolicy.getDelayUpperBound()){
            executeTick += sleep;
        } else {
            executeTick += delayRetryPolicy.getDelayUpperBound();
        }
        logger.debug("next executeCount:{}, executeTick:{}", executeCount, executeTick);
    }
}
