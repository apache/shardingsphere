package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.DelayRetryExecution;
import org.apache.zookeeper.KeeperException;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by aaa
 */
public abstract class BaseOperation implements Delayed {
    protected final IProvider provider;
    protected DelayRetryExecution retryExecution;
    
    protected BaseOperation(final IProvider provider) {
        this.provider = provider;
    }
    
    public void setRetrial(final DelayRetryExecution retryExecution){
        this.retryExecution = retryExecution;
    }
    
    @Override
    public long getDelay(TimeUnit unit) {
        long result = unit.convert(this.retryExecution.getNextTick() - System.currentTimeMillis() , TimeUnit.MILLISECONDS);
        return result;
    }
    
    /**
     * queue precedence
     */
    @Override
    public int compareTo(Delayed delayed) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
    }

    protected abstract boolean execute() throws KeeperException, InterruptedException;
    
    /*
    * @Return whether or not continue enqueue
    */
    public boolean executeOperation() throws KeeperException, InterruptedException {
        boolean result = execute();
        if (!result && retryExecution.hasNext()){
            return true;
        }
        return false;
    }
}
