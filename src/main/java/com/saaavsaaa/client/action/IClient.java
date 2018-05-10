package com.saaavsaaa.client.action;

import com.saaavsaaa.client.utility.section.Listener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.io.IOException;

/**
 * Created by aaa
 */
public interface IClient extends IAction, IGroupAction{
    void start() throws IOException, InterruptedException;
    void close() throws InterruptedException;
    Watcher registerWatch(String key, Listener listener);
    void unregisterWatch(String key);
    
    /*
    void createNamespace();
    void deleteNamespace();
    
    Watcher registerWatch(Listener listener);
    void setRootNode(String namespace);
    
    void setAuthorities(String scheme, byte[] auth);
    ZooKeeper getZooKeeper();
    */
}
