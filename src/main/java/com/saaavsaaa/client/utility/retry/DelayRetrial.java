package com.saaavsaaa.client.utility.retry;

/**
 * Created by aaa
 */
public class DelayRetrial {
    private static final int RETRY_COUNT = 29;
    private static final long BASE_DELAY = 1;
    private static final long DELAY_UPPER_BOUND = 2147483647;
    
    private final int retryCount;
    private final long baseDelay;
    private final long delayUpperBound;
    
    /*
    * Millis
    */
    public DelayRetrial(int retryCount, long baseDelay, long delayUpperBound) {
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
    
    public static DelayRetrial newNoInitDelayRetrial(){
        return new DelayRetrial(RETRY_COUNT, BASE_DELAY, DELAY_UPPER_BOUND);
    }
}
