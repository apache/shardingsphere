package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.section.Listener;
import com.saaavsaaa.client.zookeeper.strategy.StrategyType;
import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;

import java.io.IOException;

/**
 * Created by aaa
 */
public interface IClient extends IAction, IGroupAction{
    void start() throws IOException, InterruptedException;
    void close();
    void registerWatch(String key, Listener listener);
    void unregisterWatch(String key);
    void useExecStrategy(StrategyType strategyType);
    
    ZKTransaction transaction();
    /*
    void createNamespace();
    void deleteNamespace();
    
    Watcher registerWatch(Listener listener);
    void setRootNode(String namespace);
    
    void setAuthorities(String scheme, byte[] auth);
    ZooKeeper getZooKeeper();
    */
}
