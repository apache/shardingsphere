package com.saaavsaaa.client.action;

import com.saaavsaaa.client.election.LeaderElection;
import com.saaavsaaa.client.section.Listener;
import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.List;
import java.util.Stack;

/**
 * Created by aaa
 */
public interface IProvider {
    String getDataString(final String key) throws KeeperException, InterruptedException;
    byte[] getData(final String key) throws KeeperException, InterruptedException;
    void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException;
    boolean exists(final String key) throws KeeperException, InterruptedException;
    boolean exists(final String key, final Watcher watcher) throws KeeperException, InterruptedException;
    List<String> getChildren(final String key) throws KeeperException, InterruptedException;
    void create(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException;
    void update(final String key, final String value) throws KeeperException, InterruptedException;
    void delete(final String key) throws KeeperException, InterruptedException;
    void delete(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException;
    
    String getRealPath(final String path);
    List<String> getNecessaryPaths(final String key);
    Stack<String> getDeletingPaths(final String key);
    void executeContention(final LeaderElection election) throws KeeperException, InterruptedException;
    
    void createInTransaction(final String key, final String value, final CreateMode createMode, final ZKTransaction transaction) throws KeeperException, InterruptedException;
}
