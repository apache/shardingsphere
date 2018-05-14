package com.saaavsaaa.client.utility.retry;

/**
 * Created by aaa
 */
public class DelayRetrial {
    
    private final int retryCount;
    private final long baseDelay;
    private final long delayIncrease;
    
    public DelayRetrial(int retryCount, long baseDelay, long delayIncrease) {
        this.retryCount = retryCount;
        this.baseDelay = baseDelay;
        this.delayIncrease = delayIncrease;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public long getBaseDelay() {
        return baseDelay;
    }
    
    public long getDelayIncrease() {
        return delayIncrease;
    }
}
