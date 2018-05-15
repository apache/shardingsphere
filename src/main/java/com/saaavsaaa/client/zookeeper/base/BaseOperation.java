package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.utility.retry.DelayRetryExecution;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa on 18-5-14.
 */
public abstract class BaseOperation {
    protected final IProvider provider;
    
    protected DelayRetryExecution retryExecution;
    
    protected BaseOperation(final BaseProvider baseProvider) {
        this.provider = baseProvider;
    }
    
    protected abstract boolean execute() throws KeeperException, InterruptedException;
    
    /*
    * @Return whether or not continue  enqueue
    */
    public boolean executeOperation() throws KeeperException, InterruptedException {
        boolean result = execute();
        if (!result && retryExecution.hasNext()){
            return true;
        }
        return false;
    }
    
    public void setRetrial(final DelayRetryExecution retryExecution){
        this.retryExecution = retryExecution;
    }
    
    public long getNextExecuteTick() {
        return retryExecution.getNextTick();
    }
}
