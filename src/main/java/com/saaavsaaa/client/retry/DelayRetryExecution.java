package com.saaavsaaa.client.retry;

import java.util.Random;

/**
 * Created by aaa
 */
public class DelayRetryExecution {
    private final DelayRetry delayRetry;
    private final Random random;
    
    private int executeCount = 0;
    private long executeTick;
    
    public DelayRetryExecution(){
        this(DelayRetry.newNoInitDelayRetrial());
    }
    
    public DelayRetryExecution(final DelayRetry delayRetry) {
        this.delayRetry = delayRetry;
        this.executeTick = System.currentTimeMillis();
        this.random = new Random();
    }
    
    public boolean hasNext() {
        return executeCount < delayRetry.getRetryCount();
    }
    
    public long getNextTick() {
        next();
        return executeTick;
    }
    
    private void next() {
        executeCount ++;
        long sleep = delayRetry.getBaseDelay() * Math.max(1, this.random.nextInt(1 << delayRetry.getRetryCount() + 1));
        if (sleep > delayRetry.getDelayUpperBound()){
            executeTick += sleep;
        } else {
            executeTick += delayRetry.getDelayUpperBound();
        }
    }
}
