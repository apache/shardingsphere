package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.action.IStrategy;
import com.saaavsaaa.client.zookeeper.Provider;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa on 18-5-4.
 */
public abstract class BaseStrategy implements IStrategy {
    protected final Provider provider;
    public BaseStrategy(final Provider provider){
        this.provider = provider;
    }
    
    @Override
    public String getDataString(final String key) throws KeeperException, InterruptedException {
        return new String(getData(key));
    }
    
    public Provider getProvider() {
        return provider;
    }
}
