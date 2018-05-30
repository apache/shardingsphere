package com.saaavsaaa.client.zookeeper.provider;

import com.saaavsaaa.client.retry.RetryCount;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import com.saaavsaaa.client.zookeeper.base.Holder;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by aaa
 */
@Deprecated
public class RetryProvider extends BaseProvider {
    private static final Logger logger = LoggerFactory.getLogger(RetryProvider.class);
    
    public RetryProvider(String rootNode, Holder holder, boolean watched, List<ACL> authorities) {
        super(rootNode, holder, watched, authorities);
        RetryCount.INSTANCE.start();
    }
    
    // block
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        try {
            return holder.getZooKeeper().getData(key, watched, null);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryProvider SessionExpiredException getData:{}", key);
            if (RetryCount.INSTANCE.continueExecute()) {
                byte[] data = getData(key);
                RetryCount.INSTANCE.reset();
                return data;
            }
            throw ee;
        }
    }
    
    @Override
    public boolean exists(final String key) throws KeeperException, InterruptedException {
        try {
            return null != holder.getZooKeeper().exists(key, watched);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryProvider SessionExpiredException checkExists:{}", key);
            if (RetryCount.INSTANCE.continueExecute()) {
                boolean result = exists(key);
                RetryCount.INSTANCE.reset();
                return result;
            }
            throw ee;
        }
    }
    
    @Override
    public boolean exists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        try {
            return null != holder.getZooKeeper().exists(key, watcher);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryProvider SessionExpiredException checkExists:{}", key);
            if (RetryCount.INSTANCE.continueExecute()) {
                boolean result = exists(key, watcher);
                RetryCount.INSTANCE.reset();
                return result;
            }
            throw ee;
        }
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        try {
            return super.getChildren(key);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("RetryProvider SessionExpiredException getChildren:{}", key);
            if (RetryCount.INSTANCE.continueExecute()) {
                List<String> result = getChildren(key);
                RetryCount.INSTANCE.reset();
                return result;
            }
            throw ee;
        }
    }
}
