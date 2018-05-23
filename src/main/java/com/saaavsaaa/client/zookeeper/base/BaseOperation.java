package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.DelayRetryExecution;
import com.saaavsaaa.client.section.ClientContext;
import com.saaavsaaa.client.section.Connection;
import com.saaavsaaa.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by aaa
 */
public abstract class BaseOperation implements Delayed {
    private static final Logger logger = LoggerFactory.getLogger(BaseOperation.class);
    protected final ClientContext context;
    protected DelayRetryExecution retryExecution;
    
    protected BaseOperation(final ClientContext context) {
        this.context = context;
    }
    
    public void setRetrial(final DelayRetryExecution retryExecution){
        this.retryExecution = retryExecution;
    }
    
    @Override
    public long getDelay(TimeUnit unit) {
        long absoluteBlock = this.retryExecution.getNextTick() - System.currentTimeMillis();
        logger.debug("queue getDelay block:{}", absoluteBlock);
        long result = unit.convert(absoluteBlock, TimeUnit.MILLISECONDS);
        return result;
    }
    
    /**
     * queue precedence
     */
    @Override
    public int compareTo(Delayed delayed) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
    }

    protected abstract void execute() throws KeeperException, InterruptedException;
    
    /*
    * @Return whether or not continue enqueue
    */
    public boolean executeOperation() throws KeeperException, InterruptedException {
        boolean result;
        try {
            execute();
            result = true;
        } catch (KeeperException ee) {
            new Connection(context).check(ee);
            result = false;
        }
        if (!result && retryExecution.hasNext()){
            retryExecution.next();
            return true;
        }
        return false;
    }
}
