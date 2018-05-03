package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.*;

import java.util.List;

/**
 * Created by aaa
 */
public interface IClient extends IAction{

    void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException ;

    void deleteAllChildren(final String key) throws KeeperException, InterruptedException ;
    
    /*
    * delete the current node with force and delete the super node whose only child node is current node recursively
    */
    void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException ;
    
    /*
    void createNamespace();
    void deleteNamespace();
    Watcher registerWatch(String key, Listener listener);
    void unregisterWatch(String key);
    void close();
    Watcher registerWatch(Listener listener);
    void setRootNode(String namespace);
    void start();
    void setAuthorities(String scheme, byte[] auth);
    ZooKeeper getZooKeeper();
    */
}
