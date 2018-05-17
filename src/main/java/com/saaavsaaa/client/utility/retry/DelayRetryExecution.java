package com.saaavsaaa.client.utility.retry;

import java.util.Random;

/**
 * Created by aaa
 */
public class DelayRetryExecution {
    private final DelayRetrial delayRetrial;
    private final Random random;
    
    private int executeCount = 0;
    private long executeTick;
    
    public DelayRetryExecution(final DelayRetrial delayRetrial) {
        this.delayRetrial = delayRetrial;
        this.executeTick = System.currentTimeMillis();
        this.random = new Random();
    }
    
    public boolean hasNext() {
        return executeCount < delayRetrial.getRetryCount();
    }
    
    public long getNextTick() {
        next();
        return executeTick;
    }
    
    private void next() {
        executeCount ++;
        long sleep = delayRetrial.getBaseDelay() * Math.max(1, this.random.nextInt(1 << delayRetrial.getRetryCount() + 1));
        if (sleep < delayRetrial.getDelayUpperBound()){
            executeTick += sleep;
        } else {
            executeTick += delayRetrial.getDelayUpperBound();
        }
    }
}
