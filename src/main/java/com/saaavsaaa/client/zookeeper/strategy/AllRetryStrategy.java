package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.utility.retry.RetrialCenter;
import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.operation.CreateAllNeedOperation;
import com.saaavsaaa.client.zookeeper.operation.DeleteAllChildrenOperation;
import com.saaavsaaa.client.zookeeper.operation.DeleteCurrentBranchOperation;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by aaa
 */
public class AllRetryStrategy extends RetryStrategy {
    private static final Logger logger = LoggerFactory.getLogger(AllRetryStrategy.class);
    public AllRetryStrategy(String rootNode, BaseClient client, boolean watched, List<ACL> authorities) {
        super(rootNode, client, watched, authorities);
    }
    
    @Override
    public void createAllNeedPath(String key, String value, CreateMode createMode) throws KeeperException, InterruptedException {
        try {
            super.createAllNeedPath(key, value, createMode);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AllRetryStrategy SessionExpiredException CreateAllNeedOperation:{}", key);
            RetrialCenter.INSTANCE.add(new CreateAllNeedOperation(provider, key, value, createMode));
        }
    }
    
    @Override
    public void deleteAllChildren(String key) throws KeeperException, InterruptedException {
        try {
            super.deleteAllChildren(key);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AllRetryStrategy SessionExpiredException deleteAllChildren:{}", key);
            RetrialCenter.INSTANCE.add(new DeleteAllChildrenOperation(provider, key));
        }
    }
    
    @Override
    public void deleteCurrentBranch(String key) throws KeeperException, InterruptedException {
        try {
            super.deleteCurrentBranch(key);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AllRetryStrategy SessionExpiredException deleteCurrentBranch:{}", key);
            RetrialCenter.INSTANCE.add(new DeleteCurrentBranchOperation(provider, key));
        }
    }
}
