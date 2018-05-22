package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.RetryCount;
import com.saaavsaaa.client.retry.RetryPolicy;
import com.saaavsaaa.client.section.Callable;
import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by aaa
 */
public class SyncRetryStrategy extends UsualStrategy{
    private static final Logger logger = LoggerFactory.getLogger(SyncRetryStrategy.class);
    protected final RetryPolicy retryPolicy;
    protected final IClient client;
    
    public SyncRetryStrategy(final BaseClient client, final boolean watched){
        super(new BaseProvider(client, watched));
        this.client = client;
        retryPolicy = client.getContext().getRetryPolicy();
    }

    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
            Callable<byte[]> callable = new Callable() {
            @Override
            public byte[] call() throws KeeperException, InterruptedException {
                return provider.getData(provider.getRealPath(key));
            }
        };
        
        String path = provider.getRealPath(key);
        try {
            return provider.getData(path);
        } catch (KeeperException ee){
            logger.warn("AsyncRetryStrategy KeeperException getData:{}", path);
            
            if (RetryCount.INSTANCE.continueExecute()) {
                byte[] data = getData(path);
                RetryCount.INSTANCE.reset();
                return data;
            }
            throw ee;
        }
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        try {
            return provider.checkExists(path);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AsyncRetryStrategy KeeperException checkExists:{}", path);
            if (RetryCount.INSTANCE.continueExecute()) {
                boolean result = checkExists(path);
                RetryCount.INSTANCE.reset();
                return result;
            }
            throw ee;
        }
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        try {
            return provider.checkExists(path, watcher);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AsyncRetryStrategy KeeperException checkExists:{}", path);
            if (RetryCount.INSTANCE.continueExecute()) {
                boolean result = checkExists(path, watcher);
                RetryCount.INSTANCE.reset();
                return result;
            }
            throw ee;
        }
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        try {
            return provider.getChildren(path);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AsyncRetryStrategy KeeperException getChildren:{}", path);
            if (RetryCount.INSTANCE.continueExecute()) {
                List<String> result = getChildren(path);
                RetryCount.INSTANCE.reset();
                return result;
            }
            throw ee;
        }
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        Callable callable = new Callable() {
            @Override
            public Object call() throws KeeperException, InterruptedException {
                provider.createCurrentOnly(path, value, createMode);
                return null;
            }
        };
        /*try {
            
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AsyncRetryStrategy SessionExpiredException createCurrentOnly:{}", path);
        }*/
    }
}
