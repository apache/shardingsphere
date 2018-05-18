package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.utility.retry.RetrialCenter;
import com.saaavsaaa.client.utility.retry.RetryCount;
import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import com.saaavsaaa.client.zookeeper.operation.CreateCurrentOperation;
import com.saaavsaaa.client.zookeeper.operation.DeleteCurrentOperation;
import com.saaavsaaa.client.zookeeper.operation.UpdateOperation;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by aaa
 */
public class RetryStrategy extends UsualStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RetryStrategy.class);
    
    public RetryStrategy(final String rootNode, final BaseClient client, final boolean watched, final List<ACL> authorities){
        super(new BaseProvider(client, watched));
        RetryCount.INSTANCE.start();
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        try {
            return provider.getData(path);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryStrategy SessionExpiredException getData:{}", path);
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
            logger.warn("RetryStrategy SessionExpiredException checkExists:{}", path);
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
            logger.warn("RetryStrategy SessionExpiredException checkExists:{}", path);
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
            logger.warn("RetryStrategy SessionExpiredException getChildren:{}", path);
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
        try {
            provider.createCurrentOnly(path, value, createMode);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryStrategy SessionExpiredException createCurrentOnly:{}", path);
            RetrialCenter.INSTANCE.add(new CreateCurrentOperation(provider, path, value, createMode));
        }
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        try {
            provider.update(path, value);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryStrategy SessionExpiredException update:{}", path);
            RetrialCenter.INSTANCE.add(new UpdateOperation(provider, path, value));
        }
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        try {
            provider.deleteOnlyCurrent(path);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryStrategy SessionExpiredException deleteOnlyCurrent:{}", path);
            RetrialCenter.INSTANCE.add(new DeleteCurrentOperation(provider, path));
        }
    }
}
