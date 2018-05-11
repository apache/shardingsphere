package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by aaa
 * todo don't block
 */
public class RetryProvider extends BaseProvider {
    private static final Logger logger = LoggerFactory.getLogger(RetryProvider.class);
    
    RetryProvider(String rootNode, BaseClient client, boolean watched, List<ACL> authorities) {
        super(rootNode, client, watched, authorities);
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        try {
            return zooKeeper.getData(key, watched, null);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryProvider SessionExpiredException getData:{}", key);
            return getData(key);
        }
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        try {
            return null != zooKeeper.exists(key, watched);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryProvider SessionExpiredException checkExists:{}", key);
            return checkExists(key);
        }
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        try {
            return null != zooKeeper.exists(key, watcher);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryProvider SessionExpiredException checkExists:{}", key);
            return checkExists(key, watcher);
        }
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        try {
            return super.getChildren(key);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryProvider SessionExpiredException getChildren:{}", key);
            return getChildren(key);
        }
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        try {
            super.createCurrentOnly(key, value, createMode);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryProvider SessionExpiredException createCurrentOnly:{}", key);
            createCurrentOnly(key, value, createMode);
        }
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        try {
            super.update(key, value);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryProvider SessionExpiredException update:{}", key);
            update(key, value);
        }
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        try {
            super.deleteOnlyCurrent(key);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryProvider SessionExpiredException deleteOnlyCurrent:{}", key);
            deleteOnlyCurrent(key);
        }
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        try {
            super.deleteOnlyCurrent(key, callback, ctx);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryProvider SessionExpiredException deleteOnlyCurrent:{}", key);
            deleteOnlyCurrent(key, callback, ctx);
        }
    }
}
