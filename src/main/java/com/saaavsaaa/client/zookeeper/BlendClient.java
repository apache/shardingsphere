package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.strategy.BaseStrategy;
import com.saaavsaaa.client.zookeeper.strategy.ContentionStrategy;
import com.saaavsaaa.client.action.IStrategy;
import com.saaavsaaa.client.zookeeper.strategy.StrategyType;
import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by aaa on 18-5-2.
 */
public class BlendClient extends Client {
    private final Map<StrategyType, IStrategy> strategies = new ConcurrentHashMap<>();
    private IStrategy strategy;
    
    BlendClient(final String servers, final int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }

    @Override
    public synchronized void start() throws IOException, InterruptedException {
        super.start();
        UseStrategy(StrategyType.BASE);
    }
    
    public synchronized void UseStrategy(StrategyType strategyType) {
        if (strategies.containsKey(strategyType)){
            strategy = strategies.get(strategyType);
            return;
        }
        if (StrategyType.BASE == strategyType){
            strategy = new BaseStrategy(new Provider(rootNode, this, watched, authorities));
        } else {
            strategy = new ContentionStrategy(new Provider(rootNode, this, watched, authorities));
        }
        strategies.put(strategyType, strategy);
    }
    
    @Override
    public String getDataString(final String key) throws KeeperException, InterruptedException {
        return strategy.getDataString(PathUtil.getRealPath(rootNode, key));
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        return strategy.getData(PathUtil.getRealPath(rootNode, key));
    }
    
    @Override
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        strategy.getData(PathUtil.getRealPath(rootNode, key), callback, ctx);
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        return strategy.checkExists(PathUtil.getRealPath(rootNode, key));
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        return strategy.checkExists(PathUtil.getRealPath(rootNode, key), watcher);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        return strategy.getChildren(PathUtil.getRealPath(rootNode, key));
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        strategy.createCurrentOnly(PathUtil.getRealPath(rootNode, key), value, createMode);
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        strategy.createAllNeedPath(PathUtil.getRealPath(rootNode, key), value, createMode);
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        strategy.update(PathUtil.getRealPath(rootNode, key), value);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        strategy.deleteOnlyCurrent(PathUtil.getRealPath(rootNode, key));
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        strategy.deleteOnlyCurrent(PathUtil.getRealPath(rootNode, key), callback, ctx);
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        strategy.deleteAllChildren(PathUtil.getRealPath(rootNode, key));
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        strategy.deleteCurrentBranch(PathUtil.getRealPath(rootNode, key));
    }
    
    @Override
    public ZKTransaction transaction() {
        return strategy.transaction();
    }
}
