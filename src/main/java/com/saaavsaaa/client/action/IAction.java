package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * Created by aaa
 */
public interface IAction {
    String getDataString(final String key) throws KeeperException, InterruptedException;
    byte[] getData(final String key) throws KeeperException, InterruptedException;
    void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException;
    boolean checkExists(final String key) throws KeeperException, InterruptedException;
    boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException;
    List<String> getChildren(final String key) throws KeeperException, InterruptedException;
    void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException;
    void update(final String key, final String value) throws KeeperException, InterruptedException;
    void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException;
    void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException;
}
