package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.utility.retry.DelayRetrial;
import com.saaavsaaa.client.utility.retry.DelayRetryExecution;
import org.apache.zookeeper.KeeperException;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by aaa on 18-5-14.
 */
public abstract class BaseOperation implements Delayed {
    @Deprecated
    private long delayTick;
    @Deprecated
    private long executeTick;
    
    protected final IProvider provider;
    
    protected DelayRetryExecution retryExecution;
    
    protected BaseOperation(final BaseProvider baseProvider) {
        this.provider = baseProvider;
    }
    
    @Deprecated
    public void setRetrial(final DelayRetrial delayRetrial){
        this.delayTick = delayRetrial.getBaseDelay();
        this.executeTick = System.currentTimeMillis() + delayTick;
    }
    
    public void setRetrial(final DelayRetryExecution retryExecution){
        this.retryExecution = retryExecution;
    }
    
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.retryExecution.getNextTick() - System.currentTimeMillis() , TimeUnit.MILLISECONDS);
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
    
    @Deprecated
    public long getNextExecuteTick() {
        return retryExecution.getNextTick();
    }
}
