package com.saaavsaaa.client.utility.retry;

import com.saaavsaaa.client.zookeeper.base.BaseOperation;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by aaa
 */
public enum RetrialCenter {
    INSTANCE;

    private final int CAPACITY = 100;
    private final PriorityBlockingQueue<BaseOperation> queue = new PriorityBlockingQueue<>(CAPACITY, new OperationComparator());
    private DelayRetrial retrial;
    
    public void init(DelayRetrial retrial) {
        if (retrial == null) {
            this.retrial = retrial;
        }
    }
    
    public void start(){
        
    }
    
    public void add(BaseOperation operation){
        queue.put(operation);
    }
}

class OperationComparator implements Comparator<BaseOperation>{
    @Override
    public int compare(BaseOperation o1, BaseOperation o2) {
        long result = o1.getNextExecuteTick() - o2.getNextExecuteTick();
        return (int)result;
    }
}
