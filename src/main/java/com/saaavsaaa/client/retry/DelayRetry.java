package com.saaavsaaa.client.retry;

/**
 * Created by aaa
 */
public class DelayRetry {
    private static final long BASE_DELAY = 1;
    
    private final int retryCount;
    private final long baseDelay;
    private final long delayUpperBound;
    
    /*
    * Millis
    */
    public DelayRetry(int retryCount, long baseDelay) {
        this.retryCount = retryCount;
        this.baseDelay = baseDelay;
        this.delayUpperBound = Integer.MAX_VALUE;
    }
    
    public DelayRetry(int retryCount, long baseDelay, long delayUpperBound) {
        this.retryCount = retryCount;
        this.baseDelay = baseDelay;
        this.delayUpperBound = delayUpperBound;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public long getBaseDelay() {
        return baseDelay;
    }
    
    public long getDelayUpperBound() {
        return delayUpperBound;
    }
    
    public static DelayRetry newNoInitDelayRetrial(){
        return new DelayRetry(RetryCount.INSTANCE.getStandCount(), BASE_DELAY);
    }
}
