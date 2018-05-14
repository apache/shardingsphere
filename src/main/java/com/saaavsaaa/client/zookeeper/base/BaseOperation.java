package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.utility.retry.DelayRetrial;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/**
 * Created by aaa on 18-5-14.
 */
public abstract class BaseOperation {
    private final long startTick;
    protected final IProvider provider;
    
    protected int retryCount;
    private long baseDelay;
    private long delayIncrease;
    
    protected BaseOperation(final BaseProvider baseProvider) {
        this.provider = baseProvider;
        this.startTick = System.currentTimeMillis();
    }
    
    protected abstract boolean execute() throws KeeperException, InterruptedException;
    
    public void setRetrial(final DelayRetrial retrial){
        this.retryCount = retrial.getRetryCount();
        this.baseDelay = retrial.getBaseDelay();
        this.delayIncrease = retrial.getDelayIncrease();
    }
    
    public long getNextExecuteTick() {
//        nextExecuteTick
        return 0;
    }
}
