package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.retry.DelayPolicyExecutor;
import com.saaavsaaa.client.section.ClientContext;
import com.saaavsaaa.client.section.Connection;
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
    private final Connection connection;
    protected DelayPolicyExecutor delayPolicyExecutor;
    
    protected BaseOperation(final ClientContext context) {
        this.context = context;
        connection = new Connection(context);
    }
    
    public void setRetrial(final DelayPolicyExecutor delayPolicyExecutor){
        this.delayPolicyExecutor = delayPolicyExecutor;
    }
    
    @Override
    public long getDelay(TimeUnit unit) {
        long absoluteBlock = this.delayPolicyExecutor.getNextTick() - System.currentTimeMillis();
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
            connection.check(ee);
            result = false;
        }
        if (!result && delayPolicyExecutor.hasNext()){
            delayPolicyExecutor.next();
            return true;
        }
        return false;
    }
}
