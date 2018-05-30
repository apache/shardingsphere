package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.base.BaseContext;
import com.saaavsaaa.client.zookeeper.section.StrategyType;
import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by aaa
 */
public class UsualClient extends BaseClient {
    private static final Logger logger = LoggerFactory.getLogger(UsualClient.class);
    
    UsualClient(final BaseContext context) {
        super(context);
    }

    @Override
    public void start() throws IOException, InterruptedException {
        super.start();
        useExecStrategy(StrategyType.USUAL);
    }
    
    @Override
    public String getDataString(final String key) throws KeeperException, InterruptedException {
        return strategy.getDataString(key);
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        return strategy.getData(key);
    }
    
    @Override
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        strategy.getData(key, callback, ctx);
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        return strategy.checkExists(key);
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        return strategy.checkExists(key, watcher);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        return strategy.getChildren(key);
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        this.createNamespace();
        if (rootNode.equals(key)){
            return;
        }
        strategy.createCurrentOnly(key, value, createMode);
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        this.createNamespace();
        if (rootNode.equals(key)){
            return;
        }
        strategy.createAllNeedPath(key, value, createMode);
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        strategy.update(key, value);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        if (rootNode.equals(key)){
            deleteNamespace();
            return;
        }
        strategy.deleteOnlyCurrent(key);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        if (rootNode.equals(key)){
            deleteNamespace();
            return;
        }
        strategy.deleteOnlyCurrent(key, callback, ctx);
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        strategy.deleteAllChildren(key);
        if (rootNode.equals(key)){
            rootExist = false;
            logger.debug("deleteAllChildren delete root:{}", rootNode);
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        strategy.deleteCurrentBranch(key);
        if (!strategy.checkExists(rootNode)){
            rootExist = false;
            logger.debug("deleteCurrentBranch delete root:{}", rootNode);
        }
    }
    
    @Override
    public ZKTransaction transaction() {
        return new ZKTransaction(rootNode, holder);
    }
}
