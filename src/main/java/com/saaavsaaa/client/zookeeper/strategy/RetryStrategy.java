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
    
    public RetryStrategy(String rootNode, BaseClient client, boolean watched, List<ACL> authorities){
        super(new BaseProvider(rootNode, client, watched, authorities));
        RetryCount.INSTANCE.start();
    }
    
    @Override
    public byte[] getData(String key) throws KeeperException, InterruptedException {
        try {
            return provider.getData(key);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryStrategy SessionExpiredException getData:{}", key);
            if (RetryCount.INSTANCE.continueExecute()) {
                byte[] data = getData(key);
                RetryCount.INSTANCE.reset();
                return data;
            }
            throw ee;
        }
    }
    
    @Override
    public boolean checkExists(String key) throws KeeperException, InterruptedException {
        try {
            return provider.checkExists(key);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryStrategy SessionExpiredException checkExists:{}", key);
            if (RetryCount.INSTANCE.continueExecute()) {
                boolean result = checkExists(key);
                RetryCount.INSTANCE.reset();
                return result;
            }
            throw ee;
        }
    }
    
    @Override
    public boolean checkExists(String key, Watcher watcher) throws KeeperException, InterruptedException {
        try {
            return provider.checkExists(key, watcher);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryStrategy SessionExpiredException checkExists:{}", key);
            if (RetryCount.INSTANCE.continueExecute()) {
                boolean result = checkExists(key, watcher);
                RetryCount.INSTANCE.reset();
                return result;
            }
            throw ee;
        }
    }
    
    @Override
    public List<String> getChildren(String key) throws KeeperException, InterruptedException {
        try {
            return provider.getChildren(key);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryStrategy SessionExpiredException getChildren:{}", key);
            if (RetryCount.INSTANCE.continueExecute()) {
                List<String> result = getChildren(key);
                RetryCount.INSTANCE.reset();
                return result;
            }
            throw ee;
        }
    }
    
    @Override
    public void createCurrentOnly(String key, String value, CreateMode createMode) throws KeeperException, InterruptedException {
        try {
            provider.createCurrentOnly(key, value, createMode);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryStrategy SessionExpiredException createCurrentOnly:{}", key);
            RetrialCenter.INSTANCE.add(new CreateCurrentOperation(provider, key, value, createMode));
        }
    }
    
    @Override
    public void update(String key, String value) throws KeeperException, InterruptedException {
        try {
            provider.update(key, value);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryStrategy SessionExpiredException update:{}", key);
            RetrialCenter.INSTANCE.add(new UpdateOperation(provider, key, value));
        }
    }
    
    @Override
    public void deleteOnlyCurrent(String key) throws KeeperException, InterruptedException {
        try {
            provider.deleteOnlyCurrent(key);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryStrategy SessionExpiredException deleteOnlyCurrent:{}", key);
            RetrialCenter.INSTANCE.add(new DeleteCurrentOperation(provider, key));
        }
    }
}
