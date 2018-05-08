package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IExecStrategy;
import com.saaavsaaa.client.zookeeper.strategy.UsualStrategy;
import com.saaavsaaa.client.zookeeper.strategy.ContentionStrategy;
import com.saaavsaaa.client.zookeeper.strategy.StrategyType;
import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by aaa
 * todo log
 */
public class UsualClient extends Client {
    private static final Logger logger = LoggerFactory.getLogger(UsualClient.class);
    protected final Map<StrategyType, IExecStrategy> strategies = new ConcurrentHashMap<>();
    protected IExecStrategy strategy;
    
    UsualClient(final String servers, final int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }

    @Override
    public synchronized void start() throws IOException, InterruptedException {
        super.start();
        useExecStrategy(StrategyType.USUAL);
    }
    
    @Override
    public synchronized void useExecStrategy(StrategyType strategyType) {
        logger.debug("useExecStrategy:{}", strategyType);
        if (strategies.containsKey(strategyType)){
            strategy = strategies.get(strategyType);
            return;
        }
        if (StrategyType.USUAL == strategyType){
            strategy = new UsualStrategy(new Provider(rootNode, this, watched, authorities));
        } else {
            strategy = new ContentionStrategy(new Provider(rootNode, this, watched, authorities));
        }
        strategies.put(strategyType, strategy);
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
        strategy.createCurrentOnly(key, value, createMode);
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        strategy.createAllNeedPath(key, value, createMode);
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        strategy.update(key, value);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        strategy.deleteOnlyCurrent(key);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        strategy.deleteOnlyCurrent(key, callback, ctx);
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        strategy.deleteAllChildren(key);
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        strategy.deleteCurrentBranch(key);
    }
    
    @Override
    public ZKTransaction transaction() {
        return strategy.transaction();
    }
}
