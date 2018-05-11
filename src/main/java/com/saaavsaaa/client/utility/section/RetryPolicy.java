package com.saaavsaaa.client.utility.section;

/**
 * Created by aaa on 18-5-11.
 * todo
 */
public enum  RetryPolicy {
    INSTANCE;
    
    private int standCount;
    
    private static final ThreadLocal<Integer> countHolder = new ThreadLocal<>();
    private static final ThreadLocal<Long> sleepHolder = new ThreadLocal<>();
    
    public void start(){
        countHolder.set(standCount);
    }
    
    public boolean ContiueExecute(){
        int count = countHolder.get().intValue() - 1;
        if (count < 0){
            countHolder.remove();
        } else {
            countHolder.set(count);
        }
        return count > -1;
    }
}
