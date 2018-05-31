package com.saaavsaaa.client.zookeeper.section;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.DelayPolicyExecutor;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aaa
 */
public abstract class Callable<T> {
    private static final Logger logger = LoggerFactory.getLogger(Callable.class);

    protected final DelayPolicyExecutor delayPolicyExecutor;
    protected final IProvider provider;
    private T result;
    public Callable(final IProvider provider, final DelayRetryPolicy delayRetryPolicy){
        this.delayPolicyExecutor = new DelayPolicyExecutor(delayRetryPolicy);
        this.provider = provider;
    }
    public abstract void call() throws KeeperException, InterruptedException;
    
    public void setResult(T result) {
        this.result = result;
    }
    public T getResult() throws KeeperException, InterruptedException {
        if (result == null) {
            exec();
        }
        return result;
    }
    
    public void exec() throws KeeperException, InterruptedException {
        try {
            call();
        } catch (KeeperException e) {
            logger.warn("exec KeeperException:{}", e.getMessage());
            delayPolicyExecutor.next();
            if (Connection.needReset(e)){
                provider.resetConnection();
            } else {
                throw e;
            }
            execDelay();
        } catch (InterruptedException e) {
            throw e;
        }
    }
    
    protected void execDelay() throws KeeperException, InterruptedException {
        for (;;) {
            long delay = delayPolicyExecutor.getNextTick() - System.currentTimeMillis();
            if (delay > 0){
                try {
                    logger.debug("exec delay:{}", delay);
                    Thread.sleep(delay);
                } catch (InterruptedException ee) {
                    throw ee;
                }
            } else {
                if (delayPolicyExecutor.hasNext()) {
                    logger.debug("exec hasNext");
                    exec();
                }
                break;
            }
        }
    }
}
