package com.saaavsaaa.client.utility.retry;

/**
 * Created by aaa
 */
public enum  RetryCount {
    INSTANCE;
    
    private static final int RETRY_COUNT_BOUND = 29;
    private static final ThreadLocal<Integer> count = new ThreadLocal<>();
    private static final ThreadLocal<Integer> standCount = new ThreadLocal<>();
    
    public void init(final int count){
        standCount.set(count);
    }
    
    public void start(){
        count.set(getStandCount());
    }
    
    public boolean continueExecute() {
        if (count.get() == null){
            start();
        }
        int current = count.get().intValue() - 1;
        if (current < 0){
            count.remove();
        } else {
            count.set(current);
        }
        return current > -1;
    }
    
    public void reset(){
        start();
    }
    
    public int getStandCount(){
        Integer sc = standCount.get();
        if (sc == null){
            standCount.set(3);
            return 3;
        }
        if (sc > RETRY_COUNT_BOUND){
            standCount.set(RETRY_COUNT_BOUND);
        }
        return standCount.get();
    }
}
