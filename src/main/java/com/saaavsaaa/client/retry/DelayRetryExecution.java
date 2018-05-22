package com.saaavsaaa.client.retry;

import com.saaavsaaa.client.section.Callable;

import java.util.Random;

/**
 * Created by aaa
 */
public class DelayRetryExecution {
    private final RetryPolicy retryPolicy;
    private final Random random;
    
    private int executeCount = 0;
    private long executeTick;
    
    public DelayRetryExecution(){
        this(RetryPolicy.newNoInitDelayPolicy());
    }
    
    public DelayRetryExecution(final RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        this.executeTick = System.currentTimeMillis();
        this.random = new Random();
        next();
    }
    
    public boolean hasNext() {
        return executeCount < retryPolicy.getRetryCount();
    }
    
    public long getNextTick() {
        return executeTick;
    }
    
    public void next() {
        executeCount ++;
        long sleep = retryPolicy.getBaseDelay() * Math.max(1, this.random.nextInt(1 << retryPolicy.getRetryCount() + 1));
        if (sleep < retryPolicy.getDelayUpperBound()){
            executeTick += sleep;
        } else {
            executeTick += retryPolicy.getDelayUpperBound();
        }
    }
    
    public void call(final Callable callable){
        
    }
}
