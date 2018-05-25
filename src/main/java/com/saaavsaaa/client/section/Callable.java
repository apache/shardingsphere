package com.saaavsaaa.client.section;

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
    private final Connection connection;
    protected final DelayPolicyExecutor delayPolicyExecutor;
    private T result;
    public Callable(final ClientContext context){
        this.connection = new Connection(context);
        DelayRetryPolicy delayRetryPolicy = context.getDelayRetryPolicy();
        if (delayRetryPolicy == null){
            logger.warn("Callable constructor context's delayRetryPolicy is null");
            delayRetryPolicy = DelayRetryPolicy.newNoInitDelayPolicy();
        }
        this.delayPolicyExecutor = new DelayPolicyExecutor(delayRetryPolicy);
    }
    public abstract void call() throws KeeperException, InterruptedException;
    
    public void setResult(T result) {
        this.result = result;
    }
    public T getResult() throws KeeperException, InterruptedException {
        exec();
        return result;
    }
    
    public void exec() throws KeeperException, InterruptedException {
        try {
            call();
        } catch (KeeperException e) {
            logger.warn("exec KeeperException:{}", e.getMessage());
            delayPolicyExecutor.next();
            connection.check(e);
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
