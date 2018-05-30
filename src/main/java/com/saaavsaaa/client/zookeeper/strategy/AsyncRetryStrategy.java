package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.AsyncRetryCenter;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.zookeeper.operation.CreateCurrentOperation;
import com.saaavsaaa.client.zookeeper.operation.DeleteCurrentOperation;
import com.saaavsaaa.client.zookeeper.operation.UpdateOperation;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aaa
 */
public class AsyncRetryStrategy extends SyncRetryStrategy {
    private static final Logger logger = LoggerFactory.getLogger(AsyncRetryStrategy.class);
    
    public AsyncRetryStrategy(final IProvider provider, final DelayRetryPolicy delayRetryPolicy){
        super(provider, delayRetryPolicy);
        AsyncRetryCenter.INSTANCE.init(this.delayRetryPolicy);
        AsyncRetryCenter.INSTANCE.start();
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        try {
            provider.create(path, value, createMode);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AsyncRetryStrategy SessionExpiredException createCurrentOnly:{}", path);
            AsyncRetryCenter.INSTANCE.add(new CreateCurrentOperation(provider, path, value, createMode));
        }
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        try {
            provider.update(path, value);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AsyncRetryStrategy SessionExpiredException update:{}", path);
            AsyncRetryCenter.INSTANCE.add(new UpdateOperation(provider, path, value));
        }
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        try {
            provider.delete(path);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AsyncRetryStrategy SessionExpiredException deleteOnlyCurrent:{}", path);
            AsyncRetryCenter.INSTANCE.add(new DeleteCurrentOperation(provider, path));
        }
    }
}
