package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.action.IExecStrategy;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public abstract class BaseStrategy implements IExecStrategy {
    protected final BaseProvider provider;
    public BaseStrategy(final BaseProvider provider){
        this.provider = provider;
    }
    
    @Override
    public String getDataString(final String key) throws KeeperException, InterruptedException {
        return new String(getData(key));
    }
    
    public BaseProvider getProvider() {
        return provider;
    }
}
